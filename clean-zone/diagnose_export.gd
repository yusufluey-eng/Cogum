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
    var config := ConfigFile.new()
    var err := config.load("res://export_presets.cfg")
    print("DIAG: export_presets load status=", err)
    if err == OK:
        for section in config.get_sections():
            print("DIAG: section=", section)
            for key in config.get_section_keys(section):
                if key in ["name", "platform", "export_path", "package/unique_name", "architectures/arm64-v8a", "architectures/armeabi-v7a"]:
                    print("DIAG: ", key, " = ", config.get_value(section, key))
    print("DIAG: editor diagnostic script completed")
    quit()
