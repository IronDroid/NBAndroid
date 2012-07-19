/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nyerel.nbandroid.logcat.logtable;

import static org.nyerel.nbandroid.logcat.HtmlUtil.htmlEscape;

import com.android.ddmlib.Log.LogLevel;
import java.util.regex.Pattern;
import javax.swing.RowFilter;
import org.nyerel.nbandroid.logcat.LogEvent;

/**
 *
 * @author Christian Fischer
 */
public class LogLineRowFilter
    extends RowFilter<LogTableModel, Integer>
{
    private String[] searchKeyWords = new String[]{};
    private LogLevel level          = LogLevel.VERBOSE;
    private Pattern  keywordFinder  = null;
    
    
    public LogLineRowFilter() {
        setFilterString("");
    }
    
    
    /**
     * Set the filter string for this RowFilter.
     * The string will be splitted into single keywords.
     */
    public void setFilterString(String str) {
        searchKeyWords = str.toLowerCase().trim().split("\\s");
        
        // if the list contains just one empty string, replace it with an empty list.
        if (searchKeyWords.length == 1 && searchKeyWords[0].equals("")) {
            searchKeyWords = new String[0];
        }
        
        // we create a reg-ex pattern, which finds all of this keywords
        StringBuilder pattern = new StringBuilder();
        for(String keyword : searchKeyWords) {
            if (pattern.length() != 0) {
                pattern.append('|');
            }
            
            pattern.append(Pattern.quote(htmlEscape(keyword)));
        }
        
        keywordFinder = Pattern.compile('(' + pattern.toString() + ')', Pattern.CASE_INSENSITIVE);
        
        return;
    }
    
    /**
     * Get a list of all keywords in this filter.
     * @return 
     */
    public String[] getKeyWords() {
        return searchKeyWords;
    }
    
    /**
     * Set the minimum LogLevel of messages, which should be included in this filter.
     */
    public void setLogLevel(LogLevel level) {
        this.level = level;
    }
    
    /**
     * Get the current minimum LogLevel of this filter.
     * @return 
     */
    public LogLevel getLogLevel() {
        return level;
    }
    
    /**
     * Highlights all keywords in this string via HTML formatting.
     * @param str input string.
     * @return output string, containing html
     */
    public String highlight(String str) {
        if (searchKeyWords.length > 0) {
            return keywordFinder.matcher(str).replaceAll("<b>$1</b>");
        }
        
        return str;
    }
    
    @Override
    public boolean include(Entry<? extends LogTableModel, ? extends Integer> entry) {
        LogTableModel model = entry.getModel();
        LogEvent event = model.getValueAt(entry.getIdentifier());
        
        if (event.getLevel().getPriority() < level.getPriority()) {
            return false;
        }
        
        if (searchKeyWords.length > 0) {
            String message = event.getMessage().toLowerCase();
            String pname   = event.getProcessName().toLowerCase();
            String tag     = event.getTag().toLowerCase();
            
            for(String exp : searchKeyWords) {
                if (
                        !tag.contains(exp)
                     && !pname.contains(exp)
                     && !message.contains(exp)
                ) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
