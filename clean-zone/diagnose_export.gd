extends SceneTree

func _initialize() -> void:
    var settings := EditorInterface.get_editor_settings()
    for key in ["export/android/android_sdk_path", "export/android/java_sdk_path", "export/android/debug_keystore"]:
        var value := str(settings.get_setting(key))
        print("DIAG: ", key, " = ", value, " exists=", DirAccess.dir_exists_absolute(value) or FileAccess.file_exists(value))
    print("DIAG: editor diagnostic script completed")
    quit()
