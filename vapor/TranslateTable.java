package vapor;

import typecheck.*;

public class TranslateTable {
    private final Table table;
    private final Translator translator;

    public TranslateTable(Table s, Translator t) {
        table = s;
        translator = t;
    }

    public Table getTable() {
        return table;
    }

    public Translator getTranslator() {
        return translator;
    }
}
