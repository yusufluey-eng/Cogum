extends SceneTree

func _initialize() -> void:
    var settings := EditorInterface.get_editor_settings()
    for key in ["export/android/android_sdk_path", "export/android/java_sdk_path", "export/android/debug_keystore"]:
        var value := str(settings.get_setting(key))
        print("DIAG: ", key, " = ", value, " exists=", DirAccess.dir_exists_absolute(value) or FileAccess.file_exists(value))
    print("DIAG: Android SDK contents:")
    var sdk := str(settings.get_setting("export/android/android_sdk_path"))
    for subdir in ["platform-tools", "build-tools/34.0.0", "platforms/android-34", "cmdline-tools/latest"]:
        print("DIAG: ", subdir, " exists=", DirAccess.dir_exists_absolute(sdk.path_join(subdir)))
    print("DIAG: export presets:")
    for preset in EditorExport.get_export_presets():
        print("DIAG: preset=", preset.get_name(), " platform=", preset.get_platform().get_name())
        var platform := preset.get_platform()
        for method in platform.get_method_list():
            if str(method.get("name", "")).contains("export") or str(method.get("name", "")).contains("valid"):
                print("DIAG: platform method=", method.get("name"), " args=", method.get("args", []))
        for property in preset.get_property_list():
            if str(property.get("name", "")).contains("package"):
                print("DIAG: preset property=", property.get("name"))
    print("DIAG: editor diagnostic script completed")
    quit()
