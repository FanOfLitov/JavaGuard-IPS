package com.javaguard.ips.fiveg.model;


public record SliceId(int sst, String sd) {

    public SliceId {

        if (sst < 0 || sst > 255
        ) {
            throw new IllegalArgumentException("SST must be between 0 and 255");
        }
        sd = normalizeSd(sd);
    }


    private static String normalizeSd(String value) {
        if (value == null || value.isBlank()
        ) {
            return null;
        }


        String normalized = value
                        .trim()
                        .toUpperCase();


        if (
                !normalized.matches("[0-9A-F]{6}"
                )
        ) {

            throw new IllegalArgumentException(
                    "SD must contain exactly 6 hexadecimal characters"
            );
        }


        return normalized;
    }


    public String displayName() {

        if (sd == null) {

            return "SST=" + sst;
        }


        return "SST=" + sst + ", SD=" + sd;
    }
}