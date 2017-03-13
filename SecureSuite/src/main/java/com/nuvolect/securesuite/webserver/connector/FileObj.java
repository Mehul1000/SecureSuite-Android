/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package com.nuvolect.securesuite.webserver.connector;//


import com.nuvolect.securesuite.util.LogUtil;
import com.nuvolect.securesuite.util.OmniFile;
import com.nuvolect.securesuite.util.OmniImage;
import com.nuvolect.securesuite.webserver.MimeUtil;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Main file object used by elFinder.
 *<pre>
 {
 "name"   : "Images",             // (String) name of file/dir. Required
 "hash"   : "l0_SW1hZ2Vz",        // (String) hash of current file/dir path, first symbol must be letter, symbols before _underline_ - volume id, Required.
 "phash"  : "l0_Lw",              // (String) hash of parent directory. Required except roots dirs.
 "mime"   : "directory",          // (String) mime type. Required.
 "ts"     : 1334163643,           // (Number) file modification time in unix timestamp. Required.
 "date"   : "30 Jan 2010 14:25",  // (String) last modification time (mime). Depricated but yet supported. Use ts instead.
 "size"   : 12345,                // (Number) file size in bytes
 "dirs"   : 1,                    // (Number) Only for directories. Marks if directory has child directories inside it. 0 (or not set) - no, 1 - yes. Do not need to calculate amount.
 "read"   : 1,                    // (Number) is readable
 "write"  : 1,                    // (Number) is writable
 "locked" : 0,                    // (Number) is file locked. If locked that object cannot be deleted,  renamed or moved
 "tmb"    : 'bac0d45b625f8d4633435ffbd52ca495.png' // (String) Only for images. Thumbnail file name, if file do not have thumbnail yet, but it can be generated than it must have value "1"
 "alias"  : "files/images",       // (String) For symlinks only. Symlink target path.
 "thash"  : "l1_c2NhbnMy",        // (String) For symlinks only. Symlink target hash.
 "dim"    : "640x480"             // (String) For images - file dimensions. Optionally.
 "volumeid" : "l1_"               // (String) Volume id. For root dir only.
 }
 *</pre>
 */
public class FileObj {

    static JSONArray uiCmdMap = new JSONArray();
    static private boolean DEBUG = false;

    /**
     * Create and return an elFinder file object.
     * @param file
     * @param httpIpPort
     * @return
     */
    public static JSONObject makeObj(OmniFile file, String httpIpPort){

        String volumeId = file.getVolumeId();

        return makeObj(volumeId, file, httpIpPort);
    }

    /**
     * Create and return an elFinder file object.
     * @param volumeId
     * @param file
     * @param httpIpPort
     * @return
     */
    public static JSONObject makeObj(String volumeId, OmniFile file, String httpIpPort){

        JSONObject obj = new JSONObject();

        /**
         * Look for the root path.  If so there is no parent.
         * Assign null to the parentFile and do not include the phash parameter.
         */
        OmniFile parentFile =null;

        /**
         * The root file path is everything up to the root slash "/".
         */
        if( ! file.isRoot())
            parentFile = file.getParentFile();

        try {
            /**
             * Start with the file parameters
             */
            obj.put("hash", file.getHash());
            obj.put("isowner", false);//TODO confirm necessary, isowner is not documented
            obj.put("locked", file.canWrite()? 0 : 1);
            if( file.isDirectory())
                obj.put("volumeid", volumeId);

            String name = file.getName();
            obj.put("name", name);
            boolean isImage = MimeUtil.isImage(
                    FilenameUtils.getExtension( name ).toLowerCase(Locale.US));

            String mime = file.getMime();
            obj.put("mime", mime);

            if( parentFile != null)
                obj.put("phash", parentFile.getHash());

            obj.put("read", file.canRead()? 1: 0);
            obj.put("size", file.length());
            long timeStamp = file.lastModified() / 1000;
            if( DEBUG ){
                if( timeStamp < 0)
                    LogUtil.log(LogUtil.LogType.FILE_OBJ,
                        "Negative timestamp: "+ timeStamp +" for file: "+file.getName());
            }
            obj.put("ts", timeStamp);
            obj.put("write", file.canWrite()? 1: 0);
            /**
             * Normally the client uses this URL as a base to fetch thumbnails,
             * a clear text filename would be added to perform a GET.
             * This works fine for single volume, however to support multiple volumes
             * the volumeId is required. We just set the url to root and use the
             * same volumeId+hash system to fetch thumbnails.
             */
            obj.put("tmbUrl", httpIpPort + "/");

            JSONArray disabled = new JSONArray();
            obj.put("disabled",disabled);

            if( isImage){
                OmniFile thumbnailFile = OmniImage.makeThumbnail( file);
                obj.put("tmb", thumbnailFile.getHash());

                String dim = OmniImage.getDim( file);
                obj.put("dim", dim);
            }
            /**
             * Add parameters for "directory" and "volume"
             */
            if( file.isDirectory()){

                // Check if directory has any subdirectories
                boolean dirs = false;
                OmniFile[] files = file.listFiles();
                for( OmniFile afile : files){
                    if( afile.isDirectory()){

                        dirs = true;
                        break;
                    }
                }
                obj.put("dirs",dirs?"1":0);
                obj.put("volumeid",volumeId);

                if( file.isRoot()){

                    // Volume has uiCmdMap, directory does not
                    obj.put("uiCmdMap", uiCmdMap);
                }else{

                }
//                TODO test - provide url for custom directory icon.
//                 Directory has custom icon, volume does not
//                obj.put("icon", httpIpPort+"/elFinder/img/diricon.png");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}


