# TeamUp Server — API Documentation

Бэкенд мессенджера/платформы поиска команд. Spring Boot, JWT-аутентификация (Resource Server OAuth2 JWT), WebSocket (STOMP) для реального времени, PostgreSQL + MinIO (S3).

## Содержание

- [Общие соглашения](#общие-соглашения)
  - [Аутентификация](#аутентификация)
  - [Форматы](#форматы)
- [REST API](#rest-api)
  - [Auth](#auth)
  - [Users](#users)
  - [Chats](#chats)
  - [Messages](#messages)
  - [Search](#search)
- [WebSocket (STOMP)](#websocket-stomp)
  - [Подключение](#подключение)
  - [Входящие (клиент → сервер)](#входящие-клиент--сервер)
  - [Рассылки (сервер → клиент)](#рассылки-сервер--клиент)
- [Примечания и замеченные проблемы](#примечания)

---

## Общие соглашения

### Аутентификация

Все REST-эндпоинты, **кроме** `/auth/**`, требуют JWT.

Заголовок:

```
Authorization: Bearer <accessToken>
```

- Сам эндпоинт `/auth/login`, `/auth/register`, `/auth/google`, `/auth/verify-email`, `/auth/forgot` — публичные.
- Всё остальное закрыто.
- Пользователь идентифицируется по `uid` (subject токена), берётся из `Authentication.getName()`.

### Форматы

- Тело запроса/ответа — JSON (`application/json`).
- Даты в `Instant` сериализуются в ISO-8601 (UTC).
- Файлы — `multipart/form-data`, поле `file`.
- Единица времени `Instant`, в БД — `timestamptz` (Hibernate `jdbc.time_zone=UTC`).

---

## REST API

### Auth

Базовый путь: `/auth`

#### `POST /auth/register`

Регистрация. Создаёт аккаунт и отправляет письмо со ссылкой подтверждения.

Запрос:

```json
{ "email": "user@example.com", "password": "secret123" }
```

Ответ: `200`

```
Please, check your email for verification!
```

Ошибки: `400` если email уже существует.

#### `POST /auth/login`

Логин. Возвращает JWT-токен.

Запрос:

```json
{ "email": "user@example.com", "password": "secret123" }
```

Ответ: `200`

```json
{ "accessToken": "<jwt>", "tokenType": "Bearer" }
```

Ошибки: `401` — неверные учётные данные или аккаунт отключён.

#### `POST /auth/google`

Вход через Google (OAuth-верификация `id_token`).

Запрос:

```json
{ "user_id": "<google user id>", "provider_id_token": "<google id token>" }
```

Ответ: `200` — `AuthResponse` (см. выше). Если такого пользователя нет — создаётся автоматически.

#### `GET /auth/verify-email?token=<token>`

Подтверждение email через токен из письма.

Query param: `token` (string).

Ответ: `200` — `AuthResponse` (авторизует сразу после подтверждения).

Ошибки: `400` — неверный/протухший/использованный токен.

#### `POST /auth/forgot`

Запрос на сброс пароля. Отправляет письмо со ссылкой.

Запрос:

```json
{ "email": "user@example.com" }
```

Ответ: `200`

```
Please, check your email for reset password
```

---

### Users

Базовый путь: `/users`

#### `GET /users/me`

Текущий пользователь (по токену).

Ответ: `200` — `UserEntity`:

```json
{
  "uid": "1",
  "username": "matvey",
  "email": "user@example.com",
  "age": 25,
  "gender": "male",
  "description": null,
  "favouriteGame": 3,
  "online": true,
  "avatar": null
}
```

#### `GET /users/?page=<n>&limit=<n>`

Список пользователей с пагинацией.

Query params: `page` (int, обязательный), `limit` (int, обязательный).

Ответ: `200` — массив `UserEntity`.

#### `POST /users/`

Создать/до-заполнить профиль текущего пользователя.

Запрос (request body `CreateUserRequest`):

```json
{ "username": "matvey", "description": "hello", "gender": "male", "age": 25, "game": 3 }
```

Ответ: `200` — `UserEntity`.

#### `PUT /users/`

Изменить профиль текущего пользователя. Тело — то же `CreateUserRequest`, что и выше.

Ответ: `200` — `UserEntity`.

#### `GET /users/friends/{uid}`

Список друзей пользователя.

Path param: `uid` (string).

Ответ: `200` — массив `FriendshipEntity`:

```json
[ { "id": 1, "fromUser": "1", "toUser": "2", "status": true } ]
```

#### `POST /users/friends`

Отправить заявку в друзья.

Запрос:

```json
{ "fromUser": "1", "toUser": "2" }
```

Ответ: `200`.

#### `PUT /users/friends`

Принять заявку в друзья (пользователь `toUser` принимает заявку от `fromUser`).

Запрос:

```json
{ "fromUser": "1", "toUser": "2" }
```

Ответ: `200`.

#### `DELETE /users/friends`

Удалить дружбу.

Запрос:

```json
{ "fromUser": "1", "toUser": "2" }
```

Ответ: `200`.

#### `GET /users/status/{uid}`

Онлайн-статус пользователя.

Path param: `uid` (string).

Ответ: `200` — `true`/`false` (boolean). *(В коде возвращается сырой `Boolean`, не `OnlineStatusResponse` — см. [Примечания](#примечания).)*

#### `POST /users/avatars`

Загрузить аватар текущего пользователя.

Body: `multipart/form-data`, поле `file`.

Ответ: `200`.

#### `GET /users/{uid}/avatar`

URL аватара пользователя.

Path param: `uid` (string).

Ответ: `200` — string (Presigned URL на файл в S3).

#### `DELETE /users/avatars`

Удалить аватар текущего пользователя.

Ответ: `200`.

---

### Chats

Базовый путь: `/chats`

#### `GET /chats/`

Список чатов текущего пользователя.

Ответ: `200` — массив `ChatEntity`:

```json
[
  {
    "id": 1,
    "name": "Команда",
    "team": false,
    "public": false,
    "game": null,
    "createdAt": "2026-08-31T10:00:00Z",
    "icon": null
  }
]
```

#### `POST /chats/?other=<uid>`

Создать личный чат с другим пользователем.

Query param: `other` (uid string).

Ответ: `200`.

#### `GET /chats/{chatId}`

Получить чат по id (только если пользователь участник).

Path param: `chatId` (int).

Ответ: `200` — `ChatEntity`.

#### `DELETE /chats/{chatId}`

Удалить чат (только если пользователь участник).

Path param: `chatId` (int).

Ответ: `200`.

#### `GET /chats/{chatId}/reads`

Список id прочитанных сообщений в чате.

Path param: `chatId` (int).

Ответ: `200` — массив `Integer` (id прочитанных сообщений).

---

### Messages

Базовый путь: `/messages`

#### `GET /messages/{chatId}`

История сообщений чата (только для участника).

Path param: `chatId` (int).

Ответ: `200` — массив `MessageEntity`:

```json
[
  {
    "id": 1,
    "uid": "1",
    "chat": { "id": 1, ... },
    "text": "привет",
    "repliedMessage": null,
    "attachment": null,
    "sendedAt": "2026-08-31T10:00:00Z"
  }
]
```

Ошибки: `401` — не участник; `404` — чат не найден.

#### `POST /messages/{messageId}/read`

Отметить сообщение прочитанным (только участник чата). Шлёт WebSocket-событие `READ`.

Path param: `messageId` (int).

Ответ: `200`.

#### `POST /messages/{messageId}/attachment`

Загрузить вложение к сообщению (только автор сообщения).

Path param: `messageId` (int).
Body: `multipart/form-data`, поле `file`.

Ответ: `200`.

#### `GET /messages/{messageId}/attachment`

Presigned URL вложения сообщения (только участник чата).

Path param: `messageId` (int).

Ответ: `200` — string (URL).

---

### Search

Базовый путь: `/search`

#### `POST /search/`

Начать (или присоединиться к) поиску команды по параметрам. Логика сопоставляет пользователя с уже существующей «сессией поиска» по ключу `(game, age, size, gender)`; если сессии нет — создаёт новую и шлёт WebSocket-событие `NEW_USER`.

Параметры приходят query-string'ом (метод без аннотации `@RequestBody`, `SearchRequest` биндится из query/формы).

Query/форма: `game` (int), `gender` (string), `size` (int), `age` (int).

Ответ: `200` — `SearchResponse`:

```json
{
  "result": "created",           // или "found"
  "search": {
    "id": "<uuid>",
    "users": [ { ...UserEntity } ]
  }
}
```

От «found» дополнительно рассылается WebSocket-событие в канал `/topic/searches/{searchId}`.

---

## WebSocket (STOMP)

### Подключение

Эндпоинт: `GET /ws` (с поддержкой SockJS).

```
ws://<host>/ws
```

Handshake через STOMP `CONNECT` с JWT в заголовке `Authorization`:

```json
{ "Authorization": "Bearer <accessToken>" }
```

- Без токена — отказ (`BadCredentialsException`).
- Конфигурация брокера: `enableSimpleBroker("/topic")`, application prefix `/realtime`.
- При подключении пользователь становится `online=true`, при отключении — `online=false` (события `SessionConnectEvent`/`SessionDisconnectEvent`).

### Входящие (клиент → сервер)

Отправка от клиента на адреса с префиксом `/realtime/...` (секция `applicationDestinationPrefixes`).

Требуется доступ: все входящие проходят проверку членства в чате / авторства сообщения интерцепторами.

#### `SEND /realtime/chats/{chatId}/send`

Отправить сообщение в чат (только участник).

Body (`Message`):

```json
{
  "text": "привет",
  "localDateTime": "2026-08-31T13:00:00",
  "zoneId": "Europe/Moscow",
  "repliedMessage": 5
}
```

Все участники чата получают событие `SEND` в `/topic/chats/{chatId}`.

#### `SEND /realtime/messages/{messageId}/edit`

Отредактировать текст сообщения (только автор — проверка `isSender`).

Body: `Message` (см. выше).

Участники получают событие `EDIT`.

#### `SEND /realtime/messages/{messageId}/delete`

Удалить сообщение (только автор).

Участники получают событие `DELETE`.

#### `SEND /realtime/user/status/{status}`

Установить онлайн-статус текущего пользователя.

Path: `status` (boolean).

---

### Рассылки (сервер → клиент)

Подписка на `/topic/**`. Доступ к `chats` и `messages` контролируется интерцепторами членства.

| Канал | Событие | Описание |
|---|---|---|
| `SUBSCRIBE /topic/chats/{chatId}` | `MessageResponse` | Все события по чату: `SEND`, `EDIT`, `DELETE`, `READ` |
| `SUBSCRIBE /topic/searches/{searchId}` | `SearchMessage` | События поиска: `new_user`, `remove_user`, `delete_search` |

**`MessageResponse`** для `SEND`/`EDIT`:

```json
{ "event": "SEND", "message": { ...MessageEntity } }
```

**`MessageResponseById`** для `DELETE`/`READ`:

```json
{ "event": "DELETE", "messageId": 5 }
```

**`SearchMessage`**:

```json
{ "event": "new_user", "data": { ...UserEntity } }
```

**Enum значения:**
- `MessageEvent`: `SEND`, `EDIT`, `DELETE`, `READ`
- `SearchEvent`: `new_user`, `remove_user`, `delete_search`
- `SearchResult`: `created`, `found`

---

## Примечания

Замечания по текущему коду (не ошибки документации, а потенциальные баги/несоответствия):

1. `SearchController` помечен `@RestController("/search")` — аннотация с value не задаёт базовый путь. Правильнее `@RestController` + `@RequestMapping("/search")`. Аналогично в `MessageController` (`@RestController("/messages")`) и в `SearchController` метод вызывается как `searchService.search(...)`, однако в `SearchService` такой метод отсутствует (есть `getSearch`) — проект сейчас не скомпилируется.
2. `UserController.GET /users/status/{uid}` возвращает сырой `Boolean` из `userService.getOnlineStatus(uid)`, при этом есть DTO `OnlineStatusResponse(boolean isOnline)` — вероятно, ожидался он.
3. `ChatController`, `MessageController` и `SearchController` аннотированы `@Controller` (а не `@RestController`/`@Controller` с `@ResponseBody`). Проектированняя логика понятна из сервисов, но формально такие эндпоинты могут не сериализовать ответ в JSON без `@ResponseBody`.
4. Кэш `Search` и древовидная структура хранятся в памяти сервиса (`HashMap`), при рестарте/нескольких инстансах теряются.
5. Парсинг в `ChatMemberInterceptor` и `MessageInterceptor` завязан на строгий формат пути (`split("/")[2]`) — небезопасно для произвольных destination.
