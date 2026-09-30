package mekanism.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import java.util.Set;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ModelRootTransform {

    private static final Set<String> FIELDS = Set.of("translation", "rotation", "left_rotation", "scale", "right_rotation", "post-rotation", "origin");
    private static final float RADIANS = (float) Math.PI / 180;

    private ModelRootTransform() {
    }

    public static Transformation read(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsString().equals("identity")) {
            return Transformation.identity();
        }
        if (json.isJsonArray()) {
            return new Transformation(matrix(json.getAsJsonArray()));
        }
        if (!json.isJsonObject()) {
            throw new JsonParseException("Model transform must be an object, matrix or identity");
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("matrix")) {
            if (object.size() != 1) {
                throw new JsonParseException("A matrix transform cannot contain other fields");
            }
            return new Transformation(matrix(object.getAsJsonArray("matrix")));
        }
        if (!FIELDS.containsAll(object.keySet()) || object.has("rotation") && object.has("left_rotation") ||
            object.has("right_rotation") && object.has("post-rotation")) {
            throw new JsonParseException("Unknown or conflicting model transform fields");
        }
        Vector3f translation = object.has("translation") ? vector(object.get("translation")) : new Vector3f();
        Quaternionf left = rotation(object.has("rotation") ? object.get("rotation") : object.get("left_rotation"));
        Quaternionf right = rotation(object.has("right_rotation") ? object.get("right_rotation") : object.get("post-rotation"));
        JsonElement scaleValue = object.get("scale");
        Vector3f scale = scaleValue == null ? new Vector3f(1) : scaleValue.isJsonArray() ? vector(scaleValue) : new Vector3f(number(scaleValue));
        Vector3f origin = new Vector3f(1);
        if (object.has("origin")) {
            JsonElement value = object.get("origin");
            origin = value.isJsonArray() ? vector(value) : switch (value.getAsString()) {
                case "center" -> new Vector3f(0.5F);
                case "corner" -> new Vector3f();
                case "opposing-corner" -> new Vector3f(1);
                default -> throw new JsonParseException("Unknown model transform origin: " + value);
            };
        }
        Matrix4f matrix = new Matrix4f().translation(origin).mul(new Transformation(translation, left, scale, right).getMatrix())
              .translate(-origin.x, -origin.y, -origin.z);
        return new Transformation(matrix);
    }

    private static Matrix4f matrix(JsonArray rows) {
        if (rows.size() != 3) {
            throw new JsonParseException("A model matrix requires three rows");
        }
        Matrix4f matrix = new Matrix4f();
        for (int row = 0; row < 3; row++) {
            JsonArray values = rows.get(row).getAsJsonArray();
            if (values.size() != 4) {
                throw new JsonParseException("A model matrix row requires four values");
            }
            for (int column = 0; column < 4; column++) {
                matrix.set(column, row, number(values.get(column)));
            }
        }
        return matrix.determineProperties();
    }

    private static Vector3f vector(JsonElement json) {
        JsonArray values = json.getAsJsonArray();
        if (values.size() != 3) {
            throw new JsonParseException("A model transform vector requires three values");
        }
        return new Vector3f(number(values.get(0)), number(values.get(1)), number(values.get(2)));
    }

    private static Quaternionf rotation(JsonElement json) {
        if (json == null) {
            return new Quaternionf();
        }
        if (json.isJsonObject()) {
            JsonObject axis = json.getAsJsonObject();
            if (axis.size() != 1) {
                throw new JsonParseException("An axis rotation requires one axis");
            }
            var entry = axis.entrySet().iterator().next();
            float angle = number(entry.getValue()) * RADIANS;
            return switch (entry.getKey()) {
                case "x" -> new Quaternionf().rotationX(angle);
                case "y" -> new Quaternionf().rotationY(angle);
                case "z" -> new Quaternionf().rotationZ(angle);
                default -> throw new JsonParseException("Unknown rotation axis: " + entry.getKey());
            };
        }
        JsonArray values = json.getAsJsonArray();
        if (!values.isEmpty() && values.get(0).isJsonObject()) {
            Quaternionf result = new Quaternionf();
            values.forEach(value -> result.mul(rotation(value)));
            return result;
        }
        if (values.size() == 3) {
            Vector3f angles = vector(values).mul(RADIANS);
            return new Quaternionf().rotationXYZ(angles.x, angles.y, angles.z);
        }
        if (values.size() == 4) {
            return new Quaternionf(number(values.get(0)), number(values.get(1)), number(values.get(2)), number(values.get(3)));
        }
        throw new JsonParseException("A model rotation requires three angles, four quaternion values or axis rotations");
    }

    private static float number(JsonElement value) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber() || !Float.isFinite(value.getAsFloat())) {
            throw new JsonParseException("A model transform component must be a finite number");
        }
        return value.getAsFloat();
    }
}
