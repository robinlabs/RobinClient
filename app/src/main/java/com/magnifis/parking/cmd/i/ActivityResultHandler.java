package com.magnifis.parking.cmd.i;

import android.content.Intent;

public interface ActivityResultHandler {
	 boolean onActivityResult(int requestCode, int resultCode, Intent data);
}
