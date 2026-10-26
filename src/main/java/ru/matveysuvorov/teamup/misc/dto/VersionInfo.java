package ru.matveysuvorov.teamup.misc.dto;

import java.util.List;

public record VersionInfo(
        String version,
        List<String> features
) {
}
