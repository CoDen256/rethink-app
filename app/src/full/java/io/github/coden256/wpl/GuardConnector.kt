package io.github.coden256.wpl

import android.app.Activity.BIND_AUTO_CREATE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import io.github.coden.dictator.service.Guard
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class GuardConnector: ReadOnlyProperty<Any?, Guard?> {

    private var value: Guard? = null

    fun connect(context: Context){
        context.bindGuard(guardConnection(
            onConnected = {value = it},
            onDisconnected = {value = null}
        ))
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Guard? {
        return value
    }

    private fun Context.bindGuard(connection: ServiceConnection){
        val intent = Intent("io.github.coden.dictator.GUARD")
        intent.setPackage("io.github.coden.dictator")
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    private fun guardConnection(onConnected: (Guard) -> Unit, onDisconnected: () -> Unit): ServiceConnection{
        return object: ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                service ?: return
                Log.i("Guard", "Connected to Guard Service")
                onConnected(Guard.Stub.asInterface(service))
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.w("Guard", "Disconnected from Guard Service")
                onDisconnected()
            }

        }
    }
}



