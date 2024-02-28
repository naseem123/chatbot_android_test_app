/*
 * Copyright (c) ThinkResearch Inc.
 * MIT license. See LICENSE file in root directory.
 */
package com.thinkresearch.testapp.android

import android.content.Context
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugins.GeneratedPluginRegistrant

const val FLUTTER_ENGINE_ID = "flutter_engine"
/*
    This class manages the flutter engine initialization
 */

class ChannelHandler(
    context: Context, appId: String,
    origin: String,
) : FlutterPlugin, MethodCallHandler {

    private lateinit var channel: MethodChannel
    private var completables: MutableMap<String, ((MethodCall) -> Unit)> =
        mutableMapOf()

    init {
        val loader = FlutterLoader()
        loader.startInitialization(context)
        loader.ensureInitializationComplete(context, null)
        val flutterEngine = FlutterEngine(context)

        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault(),
            listOf(appId, origin)
        )
        flutterEngine.plugins.add(this)
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        FlutterEngineCache
            .getInstance()
            .put(FLUTTER_ENGINE_ID, flutterEngine)

    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "chatbot_channel")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val requestId = call.argument<String>("requestId")!!
        completables[requestId]?.invoke(call)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    fun startChatBot(context: Context) {
        context.startActivity(
            FlutterActivity
                .withCachedEngine(FLUTTER_ENGINE_ID)
                .build(context)
        )
    }

}
