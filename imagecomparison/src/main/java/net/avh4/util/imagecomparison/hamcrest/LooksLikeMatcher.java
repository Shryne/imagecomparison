package net.avh4.util.imagecomparison.hamcrest;

import net.avh4.util.imagecomparison.ImageComparison;
import net.avh4.util.imagecomparison.ImageMismatchException;
import net.avh4.util.imagecomparison.ReferenceImageNotProvidedException;
import net.avh4.util.reflection.StackUtils;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class LooksLikeMatcher extends DiagnosingMatcher<Object> {

    private final String filename;
    private final BufferedImage referenceImage;
    private final Dimension referenceImageSize;
    private final Class<?> sourceClass;

    public LooksLikeMatcher(String filename) throws IOException {
        this(filename, StackUtils
                .getCallingClass((Class<?>) ImageComparisonMatchers.class));
    }

    public LooksLikeMatcher(String filename, Class<?> callingClass)
            throws IOException {
        super();
        sourceClass = callingClass;

        this.filename = filename;
        final URL resource = sourceClass.getResource(filename);
        if (resource == null) {
            referenceImage = null;
            referenceImageSize = null;
        } else {
            referenceImage = ImageIO.read(resource);

            referenceImageSize = new Dimension(referenceImage.getWidth(),
                    referenceImage.getHeight());
        }
    }

    @Override
    protected boolean matches(Object item, Description mismatchDescription) {
        try {
            ImageComparison.assertImagesMatch(item, referenceImage);
            return true;
        } catch (ReferenceImageNotProvidedException e) {
            mismatchDescription.appendText("approval image ");
            mismatchDescription.appendText(filename);
            mismatchDescription
                    .appendText(" doesn't exist -- expected to find it in ");
            mismatchDescription.appendText(sourceClass.getPackage().getName());
            e.writeActualImageToFile(filename);
            return false;
        } catch (ImageMismatchException e) {
            mismatchDescription.appendText("images don't match: ");
            mismatchDescription.appendText(e.getLocalizedMessage());
            e.writeActualImageToFile(filename);
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        if (referenceImage == null) {
            description.appendText(
                    String.format("reference image \"%s\" to exist in %s",
                            filename, sourceClass.getPackage().getName()));
        } else {
            description.appendText(
                    String.format("something that looks like %s (%dx%d)",
                            filename, referenceImageSize.width,
                            referenceImageSize.height));
        }
    }
}