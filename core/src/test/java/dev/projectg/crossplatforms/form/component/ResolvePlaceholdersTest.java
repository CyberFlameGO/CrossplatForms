package dev.projectg.crossplatforms.form.component;

import dev.projectg.crossplatforms.Resolver;
import dev.projectg.crossplatforms.interfacing.bedrock.custom.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResolvePlaceholdersTest {

    private static final Resolver resolver = s -> s.replace("%1%", "one").replace("%two%", "2");
    
    @Test
    public void copyInputTest() {
        Input before = new Input("","type here", "words");
        Input after = before.copy();
        Assertions.assertEquals(before, after);
    }

    @Test
    public void resolveInputTest() {
        Input expected = new Input("words","one", "2");

        Input withActual = new Input("words","%1%", "%two%").withPlaceholders(resolver);
        Assertions.assertEquals(withActual, expected);

        Input setActual = new Input("words","%1%", "%two%");
        setActual.placeholders(resolver);
        Assertions.assertEquals(setActual, expected);
    }
}
