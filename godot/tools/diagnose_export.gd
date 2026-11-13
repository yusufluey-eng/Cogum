extends EditorScript

func _run() -> void:
    var platform := EditorExportPlatform.find_export_template("android_debug.apk")
    print("DIAG template lookup: ", platform)
    for p in EditorExport.get_export_platform_count():
        var ep := EditorExport.get_export_platform(p)
        print("DIAG platform: ", ep.get_name())
        if ep.get_name() == "Android":
            for i in EditorExport.get_export_preset_count():
                var preset := EditorExport.get_export_preset(i)
                if preset.get_platform() == ep:
                    var err := ""
                    var missing := false
                    print("DIAG preset: ", preset.get_name())
                    print("DIAG can_export: ", ep.can_export(preset, missing, err))
                    print("DIAG error: ", err)
