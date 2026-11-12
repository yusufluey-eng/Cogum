extends Node3D

# LAST ZONE: Godot 4 mobile FPS vertical slice.
# Left thumb: move. Right swipe: look up/down/around. FIRE: shoot. MAG: reload.
const SPEED := 4.3
const LOOK_SPEED := 0.004
var player: CharacterBody3D
var camera: Camera3D
var yaw := 0.0
var pitch := 0.0
var move_touch := -1
var look_touch := -1
var fire_touch := -1
var joy_start := Vector2.ZERO
var move_axis := Vector2.ZERO
var firing := false
var ammo := 30
var reserve := 120
var kills := 0
var enemies: Array[Node3D] = []
var hud: Label
var message: Label
var fire_button: Button
var reload_button: Button
var joystick: ColorRect
var crosshair: Label
var cooldown := 0.0

func _ready() -> void:
	_build_world()
	_build_ui()
	_refresh_hud()
	Input.mouse_mode = Input.MOUSE_MODE_VISIBLE

func _box(pos: Vector3, size: Vector3, tint: Color, solid := true) -> Node3D:
	var mesh := MeshInstance3D.new()
	var cube := BoxMesh.new()
	cube.size = size
	mesh.mesh = cube
	var mat := StandardMaterial3D.new()
	mat.albedo_color = tint
	mesh.material_override = mat
	mesh.position = pos
	add_child(mesh)
	if solid:
		var body := StaticBody3D.new()
		var shape := CollisionShape3D.new()
		var box_shape := BoxShape3D.new()
		box_shape.size = size
		shape.shape = box_shape
		mesh.add_child(body)
		body.add_child(shape)
	return mesh

func _build_world() -> void:
	_box(Vector3(0, -0.2, 0), Vector3(45, 0.4, 45), Color(0.27, 0.33, 0.30))
	for i in range(-4, 5):
		_box(Vector3(i * 4.0, 1.4, -17), Vector3(3.4, 2.8, 1), Color(0.27, 0.32, 0.35))
	_box(Vector3(-12, 1.5, 0), Vector3(1, 3, 32), Color(0.31, 0.34, 0.36))
	_box(Vector3(12, 1.5, 0), Vector3(1, 3, 32), Color(0.31, 0.34, 0.36))
	_box(Vector3(0, 1.4, 15), Vector3(25, 2.8, 1), Color(0.30, 0.34, 0.37))
	for i in range(5):
		_box(Vector3(-8 + i * 4, 0.8, -5 + (i % 2) * 5), Vector3(1.5, 1.6, 2), Color(0.38, 0.39, 0.35))
	var sun := DirectionalLight3D.new()
	sun.rotation_degrees = Vector3(-55, 25, 0)
	sun.light_energy = 1.2
	add_child(sun)
	var world := WorldEnvironment.new()
	var env := Environment.new()
	env.background_mode = Environment.BG_COLOR
	env.background_color = Color(0.17, 0.23, 0.30)
	env.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
	env.ambient_light_color = Color(0.55, 0.62, 0.70)
	world.environment = env
	add_child(world)
	player = CharacterBody3D.new()
	player.position = Vector3(0, 1.0, 9)
	add_child(player)
	var collider := CollisionShape3D.new()
	var capsule := CapsuleShape3D.new()
	capsule.height = 1.8
	capsule.radius = 0.32
	collider.shape = capsule
	player.add_child(collider)
	camera = Camera3D.new()
	camera.current = true
	camera.position.y = 0.55
	player.add_child(camera)
	var gun := MeshInstance3D.new()
	var gun_mesh := BoxMesh.new()
	gun_mesh.size = Vector3(0.14, 0.18, 0.8)
	gun.mesh = gun_mesh
	gun.position = Vector3(0.3, -0.32, -0.72)
	var gun_mat := StandardMaterial3D.new()
	gun_mat.albedo_color = Color(0.11, 0.13, 0.14)
	gun.material_override = gun_mat
	camera.add_child(gun)
	for pos in [Vector3(-7, 1, -10), Vector3(5, 1, -13), Vector3(7, 1, 0), Vector3(-4, 1, 3), Vector3(3, 1, -4)]:
		_spawn_enemy(pos)

func _spawn_enemy(pos: Vector3) -> void:
	var body := StaticBody3D.new()
	body.position = pos
	body.set_meta("health", 100)
	add_child(body)
	var shape := CollisionShape3D.new()
	var capsule := CapsuleShape3D.new()
	capsule.height = 1.8
	capsule.radius = 0.35
	shape.shape = capsule
	body.add_child(shape)
	var mesh := MeshInstance3D.new()
	var model := CapsuleMesh.new()
	model.height = 1.8
	model.radius = 0.35
	mesh.mesh = model
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.44, 0.26, 0.20)
	mesh.material_override = mat
	body.add_child(mesh)
	body.add_to_group("enemy")
	enemies.append(body)

func _build_ui() -> void:
	var layer := CanvasLayer.new()
	add_child(layer)
	var ui := Control.new()
	ui.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	ui.mouse_filter = Control.MOUSE_FILTER_IGNORE
	layer.add_child(ui)
	hud = Label.new()
	hud.position = Vector2(24, 16)
	hud.add_theme_font_size_override("font_size", 26)
	ui.add_child(hud)
	message = Label.new()
	message.position = Vector2(350, 16)
	message.text = "GOREV 01  /  KARA INIS"
	message.add_theme_font_size_override("font_size", 25)
	ui.add_child(message)
	crosshair = Label.new()
	crosshair.text = "+"
	crosshair.position = Vector2(622, 342)
	crosshair.add_theme_font_size_override("font_size", 38)
	ui.add_child(crosshair)
	joystick = ColorRect.new()
	joystick.position = Vector2(90, 500)
	joystick.size = Vector2(150, 150)
	joystick.color = Color(0.4, 0.55, 0.62, 0.20)
	joystick.mouse_filter = Control.MOUSE_FILTER_IGNORE
	ui.add_child(joystick)
	fire_button = Button.new()
	fire_button.text = "●"
	fire_button.position = Vector2(1050, 480)
	fire_button.size = Vector2(130, 130)
	fire_button.add_theme_font_size_override("font_size", 58)
	fire_button.focus_mode = Control.FOCUS_NONE
	ui.add_child(fire_button)
	fire_button.button_down.connect(func(): firing = true)
	fire_button.button_up.connect(func(): firing = false)
	reload_button = Button.new()
	reload_button.text = "MAG ↻"
	reload_button.position = Vector2(925, 605)
	reload_button.size = Vector2(110, 65)
	reload_button.focus_mode = Control.FOCUS_NONE
	ui.add_child(reload_button)
	reload_button.pressed.connect(_reload)

func _process(delta: float) -> void:
	cooldown = maxf(0.0, cooldown - delta)
	if firing and cooldown <= 0:
		_shoot()
	_refresh_hud()

func _physics_process(delta: float) -> void:
	player.rotation.y = yaw
	camera.rotation.x = pitch
	var forward := -player.global_transform.basis.z
	var right := player.global_transform.basis.x
	var direction := forward * -move_axis.y + right * move_axis.x
	player.velocity.x = direction.x * SPEED
	player.velocity.z = direction.z * SPEED
	player.velocity.y -= 16.0 * delta
	player.move_and_slide()

func _shoot() -> void:
	if ammo <= 0:
		return
	ammo -= 1
	cooldown = 0.18
	var from := camera.global_position
	var to := from - camera.global_transform.basis.z * 75.0
	var query := PhysicsRayQueryParameters3D.create(from, to)
	query.exclude = [player.get_rid()]
	var hit := get_world_3d().direct_space_state.intersect_ray(query)
	if not hit.is_empty():
		var target: Object = hit["collider"]
		if target is Node3D and target.is_in_group("enemy"):
			var health: int = target.get_meta("health") - 50
			target.set_meta("health", health)
			if health <= 0:
				target.queue_free()
				kills += 1
				if kills == 5:
					message.text = "GOREV TAMAMLANDI!"

func _reload() -> void:
	var amount := mini(30 - ammo, reserve)
	ammo += amount
	reserve -= amount

func _refresh_hud() -> void:
	hud.text = "LAST ZONE  |  HEDEF %d/5  |  %d / %d" % [kills, ammo, reserve]

func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		var t := event as InputEventScreenTouch
		if t.pressed:
			if t.position.x < 460 and t.position.y > 350 and move_touch == -1:
				move_touch = t.index
				joy_start = t.position
			elif t.position.x > 460 and t.position.x < 900 and look_touch == -1:
				look_touch = t.index
		else:
			if t.index == move_touch:
				move_touch = -1
				move_axis = Vector2.ZERO
			if t.index == look_touch:
				look_touch = -1
	elif event is InputEventScreenDrag:
		var d := event as InputEventScreenDrag
		if d.index == move_touch:
			move_axis = ((d.position - joy_start) / 90.0).limit_length(1.0)
		elif d.index == look_touch:
			yaw -= d.relative.x * LOOK_SPEED
			pitch = clampf(pitch - d.relative.y * LOOK_SPEED, -1.15, 1.15)
	elif event is InputEventMouseMotion and Input.is_mouse_button_pressed(MOUSE_BUTTON_RIGHT):
		var m := event as InputEventMouseMotion
		yaw -= m.relative.x * LOOK_SPEED
		pitch = clampf(pitch - m.relative.y * LOOK_SPEED, -1.15, 1.15)
	elif event is InputEventKey:
		var k := event as InputEventKey
		if k.pressed and k.keycode == KEY_R:
			_reload()
