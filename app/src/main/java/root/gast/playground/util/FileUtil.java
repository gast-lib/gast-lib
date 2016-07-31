/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.playground.util;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class FileUtil
{
    public static String getContentsOfReader(BufferedReader reader) throws IOException
    {
        StringBuffer contentsOfFileBuffer = new StringBuffer();
        //read it all into a string
        try 
        {
            String line = reader.readLine();
            while (line != null) 
            {
                contentsOfFileBuffer.append(line).append("\n");
                line = reader.readLine();
            }
        }
        finally
        {
            reader.close();
        }
        
        return contentsOfFileBuffer.toString();
    }
}
