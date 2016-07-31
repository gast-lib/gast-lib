package root.gast.speech.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import android.util.Log;

/**
 * helps search an index
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class LuceneIndexSearcher
{
    private static final String TAG = "LuceneIndexSearcher";
    
    private IndexSearcher searcher = null;
    
    public LuceneIndexSearcher(Directory dir) throws IOException
    {
        final boolean READ_ONLY = false;
        searcher = new IndexSearcher(dir, READ_ONLY);
    }

    public LuceneIndexSearcher(String pathToIndex, boolean useFile) throws IOException
    {
        this(makeDir(pathToIndex, useFile));
    }
    
    private static Directory makeDir(String pathToIndex, boolean useFile) throws IOException
    {
        Directory directory;
        if (useFile)
        {
            File indexFile = new File(pathToIndex);
            directory = FSDirectory.open(indexFile);
        }
        else
        {
            directory = new RAMDirectory();
        }
        return directory;
    }

    public IndexSearcher getSearcher()
    {
        return searcher;
    }

    public List<Document> getDocs(TopDocs rs, IndexSearcher searcher)
    {
        List<Document> docs = new ArrayList<Document>();
        Log.d(TAG, "num search results: " + docs.size());
        ScoreDoc[] found = rs.scoreDocs;
        float bestScore = -1.0f;
        for (ScoreDoc docFound : found)
        {
            Document doc = null;
            try
            {
                doc = searcher.doc(docFound.doc);
                docs.add(doc);
                Log.d(TAG, "doc found: " + doc + " " + docFound.score);
                if (docFound.score > bestScore)
                {
                    bestScore = docFound.score;
                }
            }
            catch (IOException e)
            {
                Log.e(TAG, "no found", e);
                continue;
            }
        }
        return docs;
    }
}
