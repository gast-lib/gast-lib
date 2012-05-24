package root.gast.speech.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import android.util.Log;

/**
 * Generic Lucene utility to help create an index
 * create this, add some documents, and then call done
 */
public class LuceneIndexBuilder
{
    private static final String TAG = "LuceneIndexBuilder";
    
    private IndexWriter writer = null;
    
    private Directory directory;

    /**
     * @param outputDir directory to put the resulting index, if null put the index in memory
     * @param toDisk if true, put index on disk, otherwise put in memory
     * @param overwrite if true, overwrite the index even if it already exists
     * @param analyzer analyzer to use to index any indexable data
     */
    public LuceneIndexBuilder(String outputDir, boolean overwrite, Analyzer analyzer)
    {
        makeDir(outputDir, overwrite);
        writer = makeWriter(directory, overwrite, analyzer);
    }

    /**
     * create an in memory index
     */
    public LuceneIndexBuilder(Analyzer analyzer)
    {
        this(null, true, analyzer);
    }
    
    private void makeDir(String outputDir, boolean overwrite)
    {
        if (outputDir != null)
        {
            if (overwrite)
            {
                Log.d(TAG, "making a new index at: " + outputDir);
                deleteExistingIndex(outputDir);
            }
            File indexFile = new File(outputDir);
            try
            {
                directory = new SimpleFSDirectory(indexFile);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error creating index", e);
            }
        }
        else
        {
            directory = new RAMDirectory();
        }
    }
    
    private IndexWriter makeWriter(Directory directory, boolean create, Analyzer analyzer)
    {
        IndexWriter writer = null;
        //create an index
        try
        {
            IndexWriterConfig config = new IndexWriterConfig(LuceneParameters.VERSION, analyzer);
            writer = new IndexWriter(directory, config);
        }
        catch (CorruptIndexException e)
        {
            Log.e(TAG, "no index build", e);
        }
        catch (LockObtainFailedException e)
        {
            Log.e(TAG, "no index build", e);
        }
        catch (IOException e)
        {
            Log.e(TAG, "no index build", e);
        }
        
        return writer;
    }


    private void deleteExistingIndex(String outputDir)
    {
        createDirIfdoesntExist(outputDir);
        //need to delete the index
        //delete all files in
        for (String file : getFileNames(outputDir))
        {
            Log.d(TAG, "deleting index file: " + file);
            File f = new File(file);
            f.delete();
        }
    }
    
    //public methods

    public void addDocument(Document doc)
    {
        try
        {
            writer.addDocument(doc);
        }
        catch (IOException e)
        {
            Log.e(TAG, "cant build index", e);
        }
    }

    /**
     * call when finished adding documents
     * to write the index and make it ready for use
     */
    public void doneWriting()
    {
        try
        {
            Log.d(TAG, "done creating index");
            writer.commit();
        }
        catch (IOException e)
        {
            Log.e(TAG, "cant build index", e);
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch (CorruptIndexException e)
            {
                Log.e(TAG, "failed to close", e);
            }
            catch (IOException e)
            {
                Log.e(TAG, "failed to close", e);
            }
        }
    }

    public Directory getDirectory()
    {
        return directory;
    }
    
    //helper methods
    
    private File createDirIfdoesntExist(String dir)
    {
        File dirFile = new File(dir);
        if (!dirFile.exists())
        {
            dirFile.mkdirs();
        }
        return dirFile;
    }

    private List<String> getFileNames(String dir)
    {
        //start with the current list
        List<String> docNames = new ArrayList<String>();

        File f = new File(dir);

        if (!f.isDirectory())
        {
            throw new RuntimeException(dir
                + " is supposed to be a directory");
        }

        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (!files[i].isDirectory())
            {
                String filePath = files[i].getAbsolutePath();
                docNames.add(filePath);
            }
        }
        
        return docNames;
    }
}
