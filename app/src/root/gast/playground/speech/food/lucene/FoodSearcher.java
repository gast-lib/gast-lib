package root.gast.playground.speech.food.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import root.gast.playground.speech.food.db.Food;
import root.gast.speech.lucene.LuceneIndexSearcher;
import root.gast.speech.lucene.LuceneParameters;
import android.util.Log;

/**
 * 
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class FoodSearcher
{
    private static final String TAG = "FoodSearcher";

    private static final int MAX_NUM_RESULTS = 10000;

    private LuceneIndexSearcher searcher;

    private Analyzer analyzer;

    public FoodSearcher(Directory dir, Analyzer analyzer) throws IOException
    {
        searcher = new LuceneIndexSearcher(dir);
        this.analyzer = analyzer;
    }

    public List<Food> findMatching(String target)
    {
        try
        {
            //Note: this creates a query using the Lucene query syntax
            //by default it OR's all terms in the query
            QueryParser parser =
                    new QueryParser(LuceneParameters.VERSION,
                            FoodDocumentTranslator.FOOD_NAME, analyzer);
            Query query = parser.parse(target);

            return executeQuery(query);
        } catch (ParseException e)
        {
            Log.e(TAG, "error", e);
            return new ArrayList<Food>();
        }
    }

    private List<Food> executeQuery(Query query)
    {
        List<Food> result = new ArrayList<Food>();

        TopDocs rs = null;
        try
        {
            Log.d(TAG, "searching with query: " + query);
            rs = searcher.getSearcher().search(query, null, MAX_NUM_RESULTS);
            Log.d(TAG, "found this many documents: " + rs.totalHits);
        } catch (IOException e)
        {
            Log.e(TAG, "failed to search", e);
            return result;
        }

        // retrieve search docs
        List<Document> docs = searcher.getDocs(rs, searcher.getSearcher());

        // convert to food objects
        for (Document document : docs)
        {
            result.add(FoodDocumentTranslator.getFood(document));
        }

        return result;
    }
}
