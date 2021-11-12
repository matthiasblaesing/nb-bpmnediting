
import java.io.FileReader;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.spi.diff.DiffProvider;

public class Test {

    public static void main(String[] args) throws Exception {
        DiffProvider pd = new BuiltInDiffProvider();
        Difference[] differences = pd.computeDiff(new FileReader("/home/matthias/tmp/old"), new FileReader("/home/matthias/tmp/new"));
        for (Difference d : differences) {
            System.out.printf("%d: %d-%d (%s) // %d-%d (%s)%n", d.getType(), d.getFirstStart(), d.getFirstEnd(), d.getFirstText(), d.getSecondStart(), d.getSecondEnd(), d.getSecondText());
        }
    }

}
