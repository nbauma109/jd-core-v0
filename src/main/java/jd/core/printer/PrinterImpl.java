package jd.core.printer;

import java.util.HashMap;
import java.util.Map;

import jd.core.DecompilationResult;
import jd.core.links.DeclarationData;
import jd.core.links.HyperlinkData;
import jd.core.links.HyperlinkReferenceData;
import jd.core.links.ReferenceData;
import jd.core.links.StringData;
import jd.core.preferences.Preferences;

public class PrinterImpl extends PlainTextPrinter {

    private final Map<String, ReferenceData> referencesCache = new HashMap<>();
    private final DecompilationResult result = new DecompilationResult();
    private final boolean realignmentLineNumber;

    public PrinterImpl(Preferences preferences) {
        setPreferences(preferences);
        this.realignmentLineNumber = preferences.getRealignmentLineNumber();
    }

    // Manage line number and misalignment
    private int textAreaLineNumber = 1;

    @Override
    public void start(int maxLineNumber, int majorVersion, int minorVersion) {
        super.start(maxLineNumber, majorVersion, minorVersion);

        if (maxLineNumber != 0) {
            result.setMaxLineNumber(maxLineNumber);
        }
    }

    @Override
    public void startOfLine(int sourceLineNumber) {
        super.startOfLine(sourceLineNumber);
        result.putLineNumber(textAreaLineNumber, sourceLineNumber);
    }

    @Override
    public void endOfLine() {
        super.endOfLine();
        textAreaLineNumber++;
    }

    @Override
    public void extraLine(int count) {
        super.extraLine(count);
        if (realignmentLineNumber && count > 0) {
            textAreaLineNumber += count;
        }
    }

    // --- Add strings --- //
    @Override
    public void printString(String s, String scopeInternalName)  {
        result.addString(new StringData(length(), s, scopeInternalName));
        super.printString(s, scopeInternalName);
    }

    // --- Add references --- //
    @Override
    public void printTypeImport(String internalName, String name) {
        addHyperlink(new HyperlinkReferenceData(length(), name.length(), newReferenceData(internalName, null, null, null)));
        super.printTypeImport(internalName, name);
    }

    @Override
    public void printType(String internalName, String name, String scopeInternalName) {
        addHyperlink(new HyperlinkReferenceData(length(), name.length(), newReferenceData(internalName, null, null, scopeInternalName)));
        super.printType(internalName, name, scopeInternalName);
    }

    @Override
    public void printField(String internalName, String name, String descriptor, String scopeInternalName) {
        addHyperlink(new HyperlinkReferenceData(length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)));
        super.printField(internalName, name, descriptor, scopeInternalName);
    }
    @Override
    public void printStaticField(String internalName, String name, String descriptor, String scopeInternalName) {
        addHyperlink(new HyperlinkReferenceData(length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)));
        super.printStaticField(internalName, name, descriptor, scopeInternalName);
    }

    @Override
    public void printConstructor(String internalName, String name, String descriptor, String scopeInternalName) {
        addHyperlink(new HyperlinkReferenceData(length(), name.length(), newReferenceData(internalName, "<init>", descriptor, scopeInternalName)));
        super.printConstructor(internalName, name, descriptor, scopeInternalName);
    }

    @Override
    public void printMethod(String internalName, String name, String descriptor, String scopeInternalName) {
        addHyperlink(new HyperlinkReferenceData(length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)));
        super.printMethod(internalName, name, descriptor, scopeInternalName);
    }
    @Override
    public void printStaticMethod(String internalName, String name, String descriptor, String scopeInternalName) {
        addHyperlink(new HyperlinkReferenceData(length(), name.length(), newReferenceData(internalName, name, descriptor, scopeInternalName)));
        super.printStaticMethod(internalName, name, descriptor, scopeInternalName);
    }

    public void addHyperlink(HyperlinkData hyperlinkData) {
        result.addHyperLink(hyperlinkData.getStartPosition(), hyperlinkData);
    }

    public ReferenceData newReferenceData(String internalName, String name, String descriptor, String scopeInternalName) {
        String key = internalName + '-' + name + '-'+ descriptor + '-' + scopeInternalName;
        return referencesCache.computeIfAbsent(key, k -> {
            ReferenceData reference = new ReferenceData(internalName, name, descriptor, scopeInternalName);
            result.addReference(reference);
            return reference;
        });
    }

    // --- Add declarations --- //
    @Override
    public void printTypeDeclaration(String internalName, String name) {
        DeclarationData data = new DeclarationData(length(), name.length(), internalName, null, null);
        result.addDeclaration(internalName, data);
        result.addTypeDeclaration(length(), data);
        super.printTypeDeclaration(internalName, name);
    }

    @Override
    public void printFieldDeclaration(String internalName, String name, String descriptor) {
        result.addDeclaration(internalName + '-' + name + '-' + descriptor, new DeclarationData(length(), name.length(), internalName, name, descriptor));
        super.printFieldDeclaration(internalName, name, descriptor);
    }
    @Override
    public void printStaticFieldDeclaration(String internalName, String name, String descriptor) {
        result.addDeclaration(internalName + '-' + name + '-' + descriptor, new DeclarationData(length(), name.length(), internalName, name, descriptor));
        super.printStaticFieldDeclaration(internalName, name, descriptor);
    }

    @Override
    public void printConstructorDeclaration(String internalName, String name, String descriptor) {
        result.addDeclaration(internalName + "-<init>-" + descriptor, new DeclarationData(length(), name.length(), internalName, "<init>", descriptor));
        super.printConstructorDeclaration(internalName, name, descriptor);
    }
    @Override
    public void printStaticConstructorDeclaration(String internalName, String name) {
        result.addDeclaration(internalName + "-<clinit>-()V", new DeclarationData(length(), name.length(), internalName, "<clinit>", "()V"));
        super.printStaticConstructorDeclaration(internalName, name);
    }

    @Override
    public void printMethodDeclaration(String internalName, String name, String descriptor) {
        result.addDeclaration(internalName + '-' + name + '-' + descriptor, new DeclarationData(length(), name.length(), internalName, name, descriptor));
        super.printMethodDeclaration(internalName, name, descriptor);
    }
    @Override
    public void printStaticMethodDeclaration(String internalName, String name, String descriptor) {
        result.addDeclaration(internalName + '-' + name + '-' + descriptor, new DeclarationData(length(), name.length(), internalName, name, descriptor));
        super.printStaticMethodDeclaration(internalName, name, descriptor);
    }

    public DecompilationResult getResult() {
        return result;
    }
}