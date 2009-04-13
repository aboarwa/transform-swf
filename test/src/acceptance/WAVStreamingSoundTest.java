package acceptance;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;


import com.flagstone.transform.factory.sound.SoundFactory;
import com.flagstone.transform.movie.Background;
import com.flagstone.transform.movie.Movie;
import com.flagstone.transform.movie.MovieTag;
import com.flagstone.transform.movie.ShowFrame;
import com.flagstone.transform.movie.datatype.Bounds;
import com.flagstone.transform.movie.datatype.ColorTable;

import org.junit.Test;

import static org.junit.Assert.fail;

public final class WAVStreamingSoundTest extends StreamingSoundTest
{
    @Test
    public void playWAV() throws IOException, DataFormatException
    {
        File sourceDir = new File("test/data/wav/reference");
        File destDir = new File("test/results/WAVStreamingSoundTest");
        
        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File directory, String name) {
                return name.endsWith(".wav");
            }
        };
        
        playSounds(sourceDir, sourceDir.list(filter), destDir);
    }
}
