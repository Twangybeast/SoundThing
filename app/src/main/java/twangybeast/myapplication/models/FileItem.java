package twangybeast.myapplication.models;

/**
 * Created by Twangybeast on 2/21/2018.
 */

public class FileItem
{
    public String fileTitle;
    public boolean isSelected;

    public FileItem(String fileTitle) {
        this.fileTitle = fileTitle;
        isSelected = false;
    }

    public void toggle()
    {
        isSelected = !isSelected;
    }
}
