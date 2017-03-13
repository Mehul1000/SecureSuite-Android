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
import com.nuvolect.securesuite.util.OmniUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/**
 * resize

 * Change the size of an image.

 * Arguments:
 *
 * cmd : resize
 * mode : 'resize' or 'crop' or 'rotate'
 * target : hash of the image path
 * width : new image width
 * height : new image height
 * x : x of crop (mode='crop')
 * y : y of crop (mode='crop')
 * degree : rotate degree (mode='rotate')
 * quality
 * Response:
 *
 * changed : (Array) of files that were successfully resized. Information about File/Directory
 * To be able to resize the image, cdc record file must be specified and resize dim.
 * Resize must be in true dim and contain a line with dimensions of height and width (like "600x400").
 * If specified without resize dim resize the dialog will not work correctly.
 *
 * GET '/servlet/connector'
 * {
 * cmd=resize,
 * mode=crop,
 * target=l0_L3N0b3JhZ2UvZW11bGF0ZWQvMC9Eb3dubG9hZC9mcm96ZW4gcm9zZSBjb3B5IDEuanBn,
 * x=86, y=35, height=504, width=512 quality=100,
 * _=1459286749082,
 * }
 */
public class CmdResize {

    public static InputStream go(Map<String, String> params) {

        String httpIpPort = params.get("url");
        String target = "";// Target is a hashed volume and path

        if( params.containsKey("target"))
            target = params.get("target");
        OmniFile targetFile = OmniUtil.getFileFromHash(target);
        LogUtil.log(LogUtil.LogType.CMD_RESIZE, "Target " + targetFile.getPath());

        String mode = "";
        if( params.containsKey("mode"))
            mode = params.get("mode");

        int x = 0;
        if( params.containsKey("x"))
            x = Integer.valueOf( params.get("x"));

        int y = 0;
        if( params.containsKey("y"))
            y = Integer.valueOf( params.get("y"));

        float degree = 0.0F;
        if( params.containsKey("degree"))
            degree = Float.valueOf( params.get("degree"));

        int height = 0;
        if( params.containsKey("height"))
            height = Integer.valueOf( params.get("height"));

        int width = 0;
        if( params.containsKey("width"))
            width = Integer.valueOf( params.get("width"));

        int quality = 100;
        if( params.containsKey("quality"))
            quality = Integer.valueOf( params.get("quality"));

        try {
            JSONArray changed = new JSONArray();

            if( mode.contains("rotate")){

                OmniFile image = OmniImage.rotateImage( targetFile, degree, quality);
                LogUtil.log(LogUtil.LogType.CMD_RESIZE,
                        "rotation complete degree: "+degree+", quality: "+quality );

                changed.put( image.getFileObject( httpIpPort ));
            }else
            if( mode.contains("crop")){

                OmniFile image = OmniImage.cropImage( targetFile, x, y, width, height, quality);
                LogUtil.log(LogUtil.LogType.CMD_RESIZE,
                        "crop complete: "+x+", "+y+", "+width+", "+height );

                changed.put( image.getFileObject( httpIpPort ));
            } else
            if( mode.contains("resize")){

                OmniFile image = OmniImage.resizeImage( targetFile, width, height, quality);
                LogUtil.log(LogUtil.LogType.CMD_RESIZE,
                        "resize complete: "+x+", "+y+", "+width+", "+height );

                changed.put( image.getFileObject( httpIpPort ));
            }
            JSONObject wrapper = new JSONObject();
            wrapper.put("changed", changed);

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
