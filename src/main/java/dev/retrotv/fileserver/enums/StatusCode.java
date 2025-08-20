package dev.retrotv.fileserver.enums;

public enum StatusCode {
      INITIALIZED("INITIALIZED")
    , UPLOADING("UPLOADING")
    , ALL_CHUNKS_UPLOADED("ALL_CHUNKS_UPLOADED")
    , MERGING("MERGING")
    , COMPLETED("COMPLETED")
    , FAILED("FAILED")
    ;

    private final String code;

    StatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean contains(String code) {
        for (StatusCode status : StatusCode.values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
