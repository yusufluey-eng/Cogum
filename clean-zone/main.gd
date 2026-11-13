extends Node3D

const MOVE_SPEED := 5.0
const LOOK_SENS := 0.004
var player: CharacterBody3D
var cam: Camera3D
var left_touch := -1
var right_touch := -1
var left_origin := Vector2.ZERO
var axis := Vector2.ZERO
var yaw := 0.0
var pitch := 0.0
var bullets := 30
var targets := 0
var hud: Label
var firing := false
var cooldown := 0.0

func _ready() -> void:
    _world()
    _ui()

func _cube(pos: Vector3, size: Vector3, color: Color) -> void:
    var body := StaticBody3D.new()
    body.position = pos
    add_child(body)
    var shape := CollisionShape3D.new()
    var box := BoxShape3D.new()
    box.size = size
    shape.shape = box
    body.add_child(shape)
    var mesh := MeshInstance3D.new()
    var visual := BoxMesh.new()
    visual.size = size
    mesh.mesh = visual
    var material := StandardMaterial3D.new()
    material.albedo_color = color
    mesh.material_override = material
    body.add_child(mesh)

func _world() -> void:
    _cube(Vector3(0, -0.3, 0), Vector3(50, 0.6, 50), Color(0.3, 0.37, 0.32))
    for i in range(7):
        _cube(Vector3(-12 + i * 4, 1.1, -11), Vector3(2, 2.2, 2), Color(0.4, 0.42, 0.44))
    var light := DirectionalLight3D.new()
    light.rotation_degrees = Vector3(-50, 25, 0)
    add_child(light)
    var env_node := WorldEnvironment.new()
    var env := Environment.new()
    env.background_mode = Environment.BG_COLOR
    env.background_color = Color(0.35, 0.48, 0.63)
    env.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
    env.ambient_light_color = Color(0.65, 0.68, 0.7)
    env_node.environment = env
    add_child(env_node)
    player = CharacterBody3D.new()
    player.position = Vector3(0, 1.1, 9)
    add_child(player)
    var collision := CollisionShape3D.new()
    var capsule := CapsuleShape3D.new()
    capsule.height = 1.8
    capsule.radius = 0.35
    collision.shape = capsule
    player.add_child(collision)
    cam = Camera3D.new()
    cam.current = true
    cam.position.y = 0.55
    player.add_child(cam)
    var gun := MeshInstance3D.new()
    var gun_box := BoxMesh.new()
    gun_box.size = Vector3(0.15, 0.16, 0.75)
    gun.mesh = gun_box
    gun.position = Vector3(0.28, -0.28, -0.65)
    cam.add_child(gun)
    for i in range(5):
        var enemy := StaticBody3D.new()
        enemy.position = Vector3(-8 + i * 4, 1, -7)
        enemy.add_to_group("enemy")
        add_child(enemy)
        var col := CollisionShape3D.new()
        var cap := CapsuleShape3D.new()
        cap.height = 1.8
        cap.radius = 0.35
        col.shape = cap
        enemy.add_child(col)
        var model := MeshInstance3D.new()
        var cap_mesh := CapsuleMesh.new()
        cap_mesh.height = 1.8
        cap_mesh.radius = 0.35
        model.mesh = cap_mesh
        enemy.add_child(model)

func _ui() -> void:
    var layer := CanvasLayer.new()
    add_child(layer)
    var root := Control.new()
    root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
    root.mouse_filter = Control.MOUSE_FILTER_IGNORE
    layer.add_child(root)
    hud = Label.new()
    hud.position = Vector2(24, 24)
    hud.add_theme_font_size_override("font_size", 28)
    root.add_child(hud)
    var aim := Label.new()
    aim.text = "+"
    aim.position = Vector2(625, 335)
    aim.add_theme_font_size_override("font_size", 40)
    root.add_child(aim)
    var fire := Button.new()
    fire.text = "●"
    fire.position = Vector2(1080, 480)
    fire.size = Vector2(130, 130)
    fire.add_theme_font_size_override("font_size", 55)
    root.add_child(fire)
    fire.button_down.connect(func(): firing = true)
    fire.button_up.connect(func(): firing = false)
    var reload := Button.new()
    reload.text = "↻"
    reload.position = Vector2(945, 590)
    reload.size = Vector2(100, 75)
    root.add_child(reload)
    reload.pressed.connect(func(): bullets = 30)

func _process(delta: float) -> void:
    cooldown = maxf(0.0, cooldown - delta)
    if firing and cooldown <= 0.0 and bullets > 0:
        _shoot()
    hud.text = "LAST ZONE  |  %d/5 TARGETS  |  %d/30" % [targets, bullets]

func _physics_process(delta: float) -> void:
    player.rotation.y = yaw
    cam.rotation.x = pitch
    var input_axis := axis
    if input_axis.length_squared() < 0.01:
        input_axis = Input.get_vector("ui_left", "ui_right", "ui_up", "ui_down")
    var direction := player.global_transform.basis.x * input_axis.x - player.global_transform.basis.z * -input_axis.y
    player.velocity.x = direction.x * MOVE_SPEED
    player.velocity.z = direction.z * MOVE_SPEED
    if player.is_on_floor():
        player.velocity.y = 0.0
    else:
        player.velocity.y -= 18.0 * delta
    player.move_and_slide()

func _shoot() -> void:
    bullets -= 1
    cooldown = 0.18
    var from := cam.global_position
    var to := from - cam.global_transform.basis.z * 80.0
    var ray := PhysicsRayQueryParameters3D.create(from, to)
    ray.exclude = [player.get_rid()]
    var hit := get_world_3d().direct_space_state.intersect_ray(ray)
    if not hit.is_empty():
        var obj: Object = hit["collider"]
        if obj is Node3D and obj.is_in_group("enemy"):
            obj.queue_free()
            targets += 1

func _input(event: InputEvent) -> void:
    if event is InputEventScreenTouch:
        var touch := event as InputEventScreenTouch
        var screen := get_viewport().get_visible_rect().size
        if touch.pressed:
            if touch.position.x < screen.x * 0.4 and touch.position.y > screen.y * 0.4 and left_touch == -1:
                left_touch = touch.index
                left_origin = touch.position
            elif touch.position.x > screen.x * 0.4 and touch.position.x < screen.x * 0.82 and right_touch == -1:
                right_touch = touch.index
        else:
            if touch.index == left_touch:
                left_touch = -1
                axis = Vector2.ZERO
            if touch.index == right_touch:
                right_touch = -1
    elif event is InputEventScreenDrag:
        var drag := event as InputEventScreenDrag
        if drag.index == left_touch:
            axis = ((drag.position - left_origin) / 90.0).limit_length(1.0)
        elif drag.index == right_touch:
            yaw -= drag.relative.x * LOOK_SENS
            pitch = clampf(pitch - drag.relative.y * LOOK_SENS, -1.2, 1.2)
