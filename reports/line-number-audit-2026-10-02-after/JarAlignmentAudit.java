import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.util.ZipLoader;
import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

public class JarAlignmentAudit {
  static final Pattern PREFIX = Pattern.compile("^/\\*\\s*(\\d+)\\s*\\*/");
  public static void main(String[] args) throws Exception {
    Path jar = Path.of(args[0]); int start = Integer.parseInt(args[1]), limit = Integer.parseInt(args[2]);
    Path out = Path.of(args[3]);
    ZipLoader zip;
    try (InputStream in = Files.newInputStream(jar)) { zip = new ZipLoader(in); }
    ClassPathLoader fallback = new ClassPathLoader();
    Loader loader = new Loader() {
      public boolean canLoad(String name) { return zip.canLoad(name) || fallback.canLoad(name); }
      public byte[] load(String name) throws IOException { return zip.canLoad(name) ? zip.load(name) : fallback.load(name); }
    };
    Preferences prefs = new Preferences(); prefs.setRealignmentLineNumber(true); prefs.setShowLineNumbers(true);
    long attempted=0, succeeded=0, errors=0, numbered=0, matched=0, mismatched=0, suppressed=0, bytecode=0, noNumbers=0;
    List<String> details = new ArrayList<>();
    int ordinal=0;
    try (JarFile jf = new JarFile(jar.toFile())) {
      for (JarEntry entry : Collections.list(jf.entries())) {
        String name=entry.getName();
        if (!name.endsWith(".class") || name.startsWith("META-INF/") || name.endsWith("module-info.class") || name.endsWith("package-info.class")) continue;
        if (ordinal++ < start) continue;
        if (attempted >= limit) break;
        attempted++;
        System.err.println("AUDIT\t"+(ordinal-1)+"\t"+name);
        PrinterImpl printer = new PrinterImpl(prefs);
        try {
          String result = printer.buildDecompiledOutput(loader, name.substring(0,name.length()-6), prefs, new DecompilerImpl());
          succeeded++; suppressed += printer.getSuppressedLineNumberCount();
          if(printer.getSuppressedLineNumberCount()>0) details.add("SUPPRESSED\t"+name+"\t"+printer.getSuppressedLineNumberCount());
          if (result.contains("// Byte code:")) { bytecode++; details.add("BYTECODE\t"+name); }
          long classNumbered=0;
          String[] lines=result.split("\\r\\n|\\n|\\r", -1);
          for(int i=0;i<lines.length;i++) {
            Matcher m=PREFIX.matcher(lines[i]);
            if(m.find()) {
              long original=Long.parseLong(m.group(1)); numbered++; classNumbered++;
              if(original==i+1) matched++;
              else { mismatched++; details.add("MISMATCH\t"+name+"\tphysical="+(i+1)+"\tsource="+original+"\t"+lines[i]); }
            }
          }
          if(classNumbered==0) noNumbers++;
        } catch(Exception | AssertionError e) { errors++; details.add("ERROR\t"+name+"\t"+e); }
      }
    }
    String counts=String.join("\t", List.of(""+attempted,""+succeeded,""+errors,""+numbered,""+matched,""+mismatched,""+suppressed,""+bytecode,""+noNumbers));
    Files.writeString(out,counts+"\n"+String.join("\n",details));
    System.out.println(counts);
  }
}
