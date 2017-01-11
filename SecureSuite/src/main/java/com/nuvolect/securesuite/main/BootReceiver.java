/*******************************************************************************
 * Copyright (c) 2011 - 2014, Nuvolect LLC. All Rights Reserved.
 * All intellectual property rights, including without limitation to
 * copyright and trademark of this work and its derivative works are
 * the property of, or are licensed to, Nuvolect LLC. 
 * Any unauthorized use is strictly prohibited.
 ******************************************************************************/
package com.nuvolect.securesuite.main;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nuvolect.securesuite.util.WorkerCommand;
import com.nuvolect.securesuite.webserver.CrypServer;
import com.nuvolect.securesuite.webserver.WebService;
import com.nuvolect.securesuite.webserver.WebUtil;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context ctx, Intent intent) {

		if(CrypServer.isServerEnabled()){

        	// Start LAN web server
        	Intent serverIntent = new Intent( ctx, WebService.class);
        	ctx.startService(serverIntent);
		}

		/**
		 * If app is on the wifi LAN, sync any pending changes.
		 */
		if( WebUtil.wifiEnabled(ctx))
			WorkerCommand.queStartIncSync(ctx);
	}
}
