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

import android.content.Context;

import com.nuvolect.securesuite.main.CConst;
import com.nuvolect.securesuite.util.LogUtil;
import com.nuvolect.securesuite.util.Omni;
import com.nuvolect.securesuite.util.OmniFile;
import com.nuvolect.securesuite.util.OmniFiles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <pre>
 * upload
 *
 * Process file upload requests. Client may request the upload of multiple files at once.
 *
 * Arguments (HTTP POST):
 *
 * cmd : upload
 * target : hash of the directory to upload to
 * upload[] : array of multipart files to upload
 * Response: An array of successfully uploaded files if success, an error otherwise.
 *
 * added : (Array) of files that were successfully uploaded. Information about File/Directory
 *
 * If the files could not be loaded, return the following:
 *
 *
 * {
 * "error" : "Unable to upload files"
 * }
 * If at least one file has been uploaded, the response is Client-Server-API-2.1 # open and * select *.
 * If not all files are uploaded, failures will be put in * error * and * errorData *:
 *
 * {
 * // open
 * "select"    : [ "8d331825ebfbe1ddae14d314bf81a712" ], // (Array)  An array of hashes of the loaded paths
 * "error"     : "Some files was not uploaded",          // (String) If not all files have been uploaded
 * "errorData" : {                                       // (Object) Info about the files that could not be uploaded
 * "some-file.exe" : "Not allowed file type"             // (String) "filename": "error"
 * }
 * }
 *
 * POST DATA:
 * ------WebKitFormBoundary79xNBbwg7czVfc7l
 Content-Disposition: form-data; name="cmd"

 upload
 ------WebKitFormBoundary79xNBbwg7czVfc7l
 Content-Disposition: form-data; name="target"

 l2_Lw
 ------WebKitFormBoundary79xNBbwg7czVfc7l
 Content-Disposition: form-data; name="upload[]"; filename="Very Nice.txt"
 Content-Type: text/plain

 With nice content.
 ------WebKitFormBoundary79xNBbwg7czVfc7l--

 Files over 10MB are chunked

 0 = {java.util.HashMap$HashMapEntry@5093} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5094} "chunk" -> "Kepler jumpy puppy.mp4.0_4.part"
 2 = {java.util.HashMap$HashMapEntry@5095} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5096} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5097} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5098} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5099} "range" -> "0,10485760,47858929"
 7 = {java.util.HashMap$HashMapEntry@5100} "queryParameterStrings" -> "null"

 0 = {java.util.HashMap$HashMapEntry@5124} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5125} "chunk" -> "Kepler jumpy puppy.mp4.1_4.part"
 2 = {java.util.HashMap$HashMapEntry@5126} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5127} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5128} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5129} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5130} "range" -> "10485760,10485760,47858929"
 7 = {java.util.HashMap$HashMapEntry@5131} "queryParameterStrings" -> "null"

 0 = {java.util.HashMap$HashMapEntry@5156} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5157} "chunk" -> "Kepler jumpy puppy.mp4.3_4.part"
 2 = {java.util.HashMap$HashMapEntry@5158} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5159} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5160} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5161} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5162} "range" -> "31457280,10485760,47858929"
 7 = {java.util.HashMap$HashMapEntry@5163} "queryParameterStrings" -> "null"

 0 = {java.util.HashMap$HashMapEntry@5188} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5189} "chunk" -> "Kepler jumpy puppy.mp4.4_4.part"
 2 = {java.util.HashMap$HashMapEntry@5190} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5191} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5192} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5193} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5194} "range" -> "41943040,5915889,47858929"
 7 = {java.util.HashMap$HashMapEntry@5195} "queryParameterStrings" -> "null"

 0 = {java.util.HashMap$HashMapEntry@5222} "cmd" -> "upload"
 1 = {java.util.HashMap$HashMapEntry@5223} "chunk" -> "Kepler jumpy puppy.mp4.2_4.part"
 2 = {java.util.HashMap$HashMapEntry@5224} "target" -> "l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Nb3ZpZXMvS2VwbGVy"
 3 = {java.util.HashMap$HashMapEntry@5225} "cid" -> "1463563961531"
 4 = {java.util.HashMap$HashMapEntry@5226} "upload[]" -> "blob"
 5 = {java.util.HashMap$HashMapEntry@5227} "upload_path[]" ->
 6 = {java.util.HashMap$HashMapEntry@5228} "range" -> "20971520,10485760,47858929"
 7 = {java.util.HashMap$HashMapEntry@5229} "queryParameterStrings" -> "null"



 * </pre>
 */
public class CmdUpload {

    private static boolean DEBUG = false; //LogUtil.DEBUG;
    /**
     * Keep a list of all chunks that have been uploaded.
     * For multi-file upload, chunks can be mixed from different files.
     * Chunks arrive in random order.
     * Count chunks matching the base filename to know when all chunks
     * are uploaded for a specific file.
     */
    private static ArrayList<String> m_fileChunks = new ArrayList<>();

    public static synchronized void init(){

        m_fileChunks = new ArrayList<>();
    }

    public static synchronized ByteArrayInputStream go(Context ctx, Map<String, String> params) {

        String httpIpPort = params.get("url");
        String target = params.get("target");
        String error = "";

        // Get file to target directory
        OmniFile targetDirectory = new OmniFile(target);
        if( ! targetDirectory.isDirectory())
            error = "Unable to upload files";
        String destPath = targetDirectory.getPath();
        String targetVolumeId = targetDirectory.getVolumeId();
        String defaultVolumeId = Omni.getDefaultVolumeId();

        String chunk = "";
        int chunkNumber = 0;
        int chunkMax = 0;
        String[] parts = new String[0];
        if( params.containsKey("chunk")){

            chunk = params.get("chunk");

            parts = chunk.split(Pattern.quote("."));
            String countOfMax = parts[ parts.length-2];
            String[] twoNumbers = countOfMax.split("_");
            chunkNumber = Integer.valueOf( twoNumbers[0]);
            chunkMax = Integer.valueOf( twoNumbers[1]);
        }

        JSONArray added = new JSONArray();
        JSONObject wrapper = new JSONObject();

        try {

            /**
             * Parse the uploads array and copy from temporary storage each
             * file to the destination folder.
             * Delete temporary files.
             */
            JSONArray postUploads = new JSONArray(params.get("post_uploads"));

            for( int i = 0; i < postUploads.length(); i++){

                JSONObject postUpload = postUploads.getJSONObject(i);
                String uploadFileName = postUpload.getString(CConst.FILE_NAME);
                String filePath = postUpload.getString(CConst.FILE_PATH);
                String chunkFileName = "";

                if( ! chunk.isEmpty()){

                    uploadFileName = chunk;
                    destPath = ctx.getFilesDir().getPath();

                    // Assemble the base filename, ex: puppy.mp4.2_4.part >> puppy.mp4
                    String period = "";
                    for( int j = 0; j < parts.length-2; j++){

                        chunkFileName += period + parts[ j ];
                        period = ".";
                    }
                }

                OmniFile srcFile = new OmniFile( defaultVolumeId, filePath);
                OmniFile destFile = new OmniFile(
                        !chunk.isEmpty()? defaultVolumeId:targetVolumeId,
                        destPath +"/"+ uploadFileName);

                error = OmniFiles.copyFile(srcFile, destFile)?"":"File copy failure";

                if( ! chunk.isEmpty()){

                    m_fileChunks.add( chunk );

                    for( String s : m_fileChunks)
                        LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "fileChunks add: " + s);
                }


                // No need to delete temporary files, nanohttpd will do this automatically

                if( error.isEmpty()){

                    /**
                     * See if the last chunk is complete.
                     * If so assemble the chunks back into the entire file.
                     */
                    int chunkMatchCount = 0;
                    for( String c : m_fileChunks){

                        if( c.startsWith( chunkFileName)){
                            ++chunkMatchCount;
                            if( chunkMatchCount == chunkMax+1)
                                break;
                        }
                    }
                    boolean allChunksUploaded = chunkMatchCount == chunkMax+1;// chunk names start at 0

                    if( ! chunk.isEmpty() && allChunksUploaded){

                        LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "File upload success, all chunks: " + destFile.getPath());

                        // Reset target path from internal storage to the desired path
                        String sourcePath = ctx.getFilesDir().getPath();
                        destPath = targetDirectory.getPath();

                        try {
                        /**
                         * Open the target file.
                         * Iterate and append each file to the target file.
                         */
                        destFile = new OmniFile( targetDirectory.getVolumeId(), destPath + "/" + chunkFileName);
                        OutputStream destOutputStream = destFile.getOutputStream();
                        int totalSize = 0;

                            for( int j = 0; j <= chunkMax; j++){

                                String sourceName = chunkFileName +"."+j+"_"+chunkMax+".part";
                                OmniFile sourceFile = new OmniFile( defaultVolumeId,
                                        sourcePath + "/" + sourceName);

                                int bytesCopied = OmniFiles.copyFileLeaveOutOpen(
                                        sourceFile.getFileInputStream(),
                                        destOutputStream);

                                totalSize += bytesCopied;
                                if( DEBUG )
                                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "Bytes copied, total: "
                                        + bytesCopied + ", "+totalSize);

                                // Remove record of the chunk
                                String sourceFileName = sourceFile.getName();
                                // This is a clever way to create the iterator and call iterator.hasNext() like
                                // you would do in a while-loop. It would be the same as doing:
                                //     Iterator<String> iterator = list.iterator();
                                //     while (iterator.hasNext()) {
                                for (Iterator<String> iterator = m_fileChunks.iterator(); iterator.hasNext();) {
                                    String string = iterator.next();
                                    if (string.contentEquals( sourceFileName)) {
                                        // Remove the current element from the iterator and the list.
                                        iterator.remove();
                                    }
                                }

                                LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "Removed " + sourceFile.getName());
                                for( String s : m_fileChunks)
                                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "fileChunks remove: " + s);
                                // Delete temp file
                                if( ! sourceFile.delete() ){

                                    error = "Delete temp file failed : "+sourceFile.getPath();
                                    break;
                                }
                            }

                            destOutputStream.flush();
                            destOutputStream.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    JSONObject fileObj = FileObj.makeObj(targetVolumeId, destFile, httpIpPort);
                    added.put(fileObj);
                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "File upload success: " + destFile.getPath());
                }
                else{
                    wrapper.put("error", error);
                    LogUtil.log(LogUtil.LogType.CMD_UPLOAD, "File upload error: " + error);
                    break;
                }
            }
            wrapper.put("added", added);

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
