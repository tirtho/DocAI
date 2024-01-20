package com.azure.spring.samples.utils;

public enum FileType {
	JPG(FileCategory.IMAGE), JPEG(FileCategory.IMAGE), GIF(FileCategory.IMAGE), PNG(FileCategory.IMAGE),
	BMP(FileCategory.IMAGE), TIFF(FileCategory.IMAGE), HEIF(FileCategory.IMAGE), WEBP(FileCategory.IMAGE),
	MP4(FileCategory.VIDEO), MOV(FileCategory.VIDEO), MSG(FileCategory.DOC), PDF(FileCategory.DOC),
	TXT(FileCategory.TEXT), DOCX(FileCategory.IMAGE), DOC(FileCategory.DOC), RTF(FileCategory.DOC),
	XLS(FileCategory.DOC), XLSX(FileCategory.DOC), PPT(FileCategory.DOC), PPTX(FileCategory.DOC);

	public static enum FileCategory {
		IMAGE, VIDEO, AUDIO, TEXT, DOC;
	}

	public final FileCategory thisFileCategory;
	
	FileType(FileCategory fileCategory) {
		this.thisFileCategory = fileCategory;
	}
	
    public static FileType fromFilename(String filename) {
        for (FileType theFileType : FileType.values()) {
            if (filename.toLowerCase().endsWith(theFileType.name().toLowerCase())) {
                return theFileType;
            }
        }
        return null;
    }
	
	public FileCategory getFileCategory() {
		return this.thisFileCategory;
	}
	
	public boolean isImage() {
		if (getFileCategory().compareTo(FileCategory.IMAGE) == 0)
				return true;
        return false;
	}
	
	public boolean isVideo() {
		if (getFileCategory().compareTo(FileCategory.VIDEO) == 0)
			return true;
    return false;
	}

	public boolean isAudio() {
		if (getFileCategory().compareTo(FileCategory.AUDIO) == 0)
			return true;
    return false;
	}

	public boolean isDocument() {
		if (getFileCategory().compareTo(FileCategory.DOC) == 0)
			return true;
    return false;
	}

	public boolean isText() {
		if (getFileCategory().compareTo(FileCategory.TEXT) == 0)
			return true;
    return false;
	}

}
