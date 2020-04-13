package com.log3900.shared.architecture

import android.content.Context
import android.content.DialogInterface

class DialogEventMessage(var title: String, var message: String, var positiveButtonListener: ((dialog: DialogInterface, which: Int) -> Unit)?,
                          var negativeButtonListener: ((dialog: DialogInterface, which: Int) -> Unit)?)