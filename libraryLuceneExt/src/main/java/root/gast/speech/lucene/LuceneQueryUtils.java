package root.gast.speech.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

public class LuceneQueryUtils
{ 
    public static Query makeQueryFor(String text, String field,
            Analyzer analyzer) throws ParseException
    {
        QueryParser parser = new QueryParser(LuceneParameters.VERSION, field, analyzer);
        Query query = parser.parse(text);
        return query;
    }
}
