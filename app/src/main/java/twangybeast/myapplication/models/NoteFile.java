package twangybeast.myapplication.models;

/**
 * Created by cHeNdAn19 on 3/6/2018.
 */

public class NoteFile extends FileItem
{
    public String lastModified;
    public NoteFile(String noteTitle, String lastModified)
    {
        super(noteTitle);
        this.lastModified = lastModified;
    }
}