package acceptance;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { ShowAWTFontTest.class, ShowTrueTypeFontTest.class })
@SuppressWarnings("PMD.AtLeastOneConstructor")
public final class AllFontTests {
}
