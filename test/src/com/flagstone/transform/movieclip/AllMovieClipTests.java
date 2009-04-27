package com.flagstone.transform.movieclip;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { DefineMovieClipTest.class,
		InitializeMovieClipTest.class, QuicktimeMovieTest.class, })
@SuppressWarnings("PMD.AtLeastOneConstructor")
public final class AllMovieClipTests {
}
