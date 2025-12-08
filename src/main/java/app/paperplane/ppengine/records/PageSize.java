package app.paperplane.ppengine.records;

import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.Size2DSyntax;

// page size record that stores page sizes in width (mm) and height (mm)
public record PageSize(float width, float height) {
    public static final PageSize A4 = new PageSize(210.0f, 297.0f);
    public static final PageSize A3 = new PageSize(297.0f, 420.0f);
    public static final PageSize A5 = new PageSize(148.0f, 210.0f);
    public static final PageSize LETTER = new PageSize(216.0f, 279.0f);
    public static final PageSize LEGAL = new PageSize(216.0f, 356.0f);
    public static final PageSize C = new PageSize(431.9f, 558.8f);

    public MediaSizeName toMediaSizeName() {

        if (matches(this, A4)) return MediaSizeName.ISO_A4;
        if (matches(this, A3)) return MediaSizeName.ISO_A3;
        if (matches(this, A5)) return MediaSizeName.ISO_A5;
        if (matches(this, LETTER)) return MediaSizeName.NA_LETTER;
        if (matches(this, LEGAL)) return MediaSizeName.NA_LEGAL;
        if (matches(this, C)) return MediaSizeName.C;

        // fallback for custom sizes
        MediaSize custom = new MediaSize(width, height, Size2DSyntax.MM);
        return custom.getMediaSizeName();
    }

    private static boolean matches(PageSize a, PageSize b) {
        return Math.abs(a.width - b.width) < 0.5f &&
                Math.abs(a.height - b.height) < 0.5f;
    }
}