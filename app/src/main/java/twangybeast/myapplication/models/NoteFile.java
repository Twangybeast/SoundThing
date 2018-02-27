package twangybeast.myapplication.models;

/**
 * Created by Twangybeast on 2/21/2018.
 */

public class NoteFile
{
    public String noteTitle;
    public String lastModified;
    public boolean isSelected;
    public NoteFile(String noteTitle, String lastModified)
    {
        this.noteTitle = noteTitle;
        this.lastModified = lastModified;
        isSelected = false;
    }
    public void toggle()
    {
        isSelected = !isSelected;
    }
}
