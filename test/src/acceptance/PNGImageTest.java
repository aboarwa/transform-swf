package acceptance;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

import java.util.zip.DataFormatException;


import com.flagstone.transform.factory.image.ImageFactory;
import com.flagstone.transform.movie.Background;
import com.flagstone.transform.movie.ImageTag;
import com.flagstone.transform.movie.Movie;
import com.flagstone.transform.movie.MovieTag;
import com.flagstone.transform.movie.Place2;
import com.flagstone.transform.movie.ShowFrame;
import com.flagstone.transform.movie.datatype.ColorTable;
import com.flagstone.transform.movie.linestyle.LineStyle;
import com.flagstone.transform.movie.shape.DefineShape3;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.Ignore;

public final class PNGImageTest extends ImageTest
{
    @Test
    public void showPNG() throws IOException, DataFormatException
    {
        File sourceDir = new File("test/data/png/reference");
        File destDir = new File("test/results/PNGImageTest");
     
        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File directory, String name) {
                return name.endsWith(".png");
            }
        };
        
        showFiles(sourceDir, sourceDir.list(filter), destDir);
    }
}
