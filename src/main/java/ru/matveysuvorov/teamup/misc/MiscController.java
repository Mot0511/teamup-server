package ru.matveysuvorov.teamup.misc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.matveysuvorov.teamup.misc.dto.VersionInfo;

import java.util.List;

@RestController
@RequestMapping("/misc")
public class MiscController {

    @Value("${app.version}") String version;

    @GetMapping("/version")
    public VersionInfo getVersionInfo() {
        return new VersionInfo(
                version,
                List.of(
                        "Повышение стабильности приложения",
                        "Повышение безопасности приложения",
                        "Быстрый поиск теперь работает по возрастным категориям, а не по конкретным возрастам",
                        "Подтверждение почты теперь по коду, а не по ссылке"
                )
        );
    }

}
