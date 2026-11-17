@tool
extends EditorScript

func _run() -> void:
    print("DIAG: android_sdk=", EditorInterface.get_editor_settings().get_setting("export/android/android_sdk_path"))
    print("DIAG: java_sdk=", EditorInterface.get_editor_settings().get_setting("export/android/java_sdk_path"))
    var exports = EditorExport.get_singleton()
    print("DIAG: platforms=", exports.get_export_platform_count())
    print("DIAG: presets=", exports.get_export_preset_count())
    for i in range(exports.get_export_preset_count()):
        var preset = exports.get_export_preset(i)
        var platform = preset.get_platform()
        print("DIAG: preset=", preset.get_name(), " platform=", platform.get_name())
        print("DIAG: config_valid=", platform.has_valid_export_configuration(preset, true))
        print("DIAG: project_valid=", platform.has_valid_project_configuration(preset))
        for option in platform.get_export_options():
            var key = option["name"]
            var warning = platform.get_export_option_warning(preset, key)
            if warning != "":
                print("DIAG: option ", key, ": ", warning)
