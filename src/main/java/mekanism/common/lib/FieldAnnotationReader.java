package mekanism.common.lib;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public final class FieldAnnotationReader {

    private FieldAnnotationReader() {
    }

    public static List<AnnotationData> readFieldAnnotations(Path path) throws IOException {
        ClassNode node = new ClassNode();
        try (InputStream input = Files.newInputStream(path)) {
            new ClassReader(input).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        List<AnnotationData> result = new ArrayList<>();
        for (FieldNode field : node.fields) {
            collectAnnotations(result, node.name, field.name, field.visibleAnnotations);
            collectAnnotations(result, node.name, field.name, field.invisibleAnnotations);
        }
        return result;
    }

    private static void collectAnnotations(List<AnnotationData> result, String owner, String field, @Nullable List<AnnotationNode> annotations) {
        if (annotations == null) {
            return;
        }
        for (AnnotationNode annotation : annotations) {
            Map<String, Object> values = new Object2ObjectOpenHashMap<>();
            if (annotation.values != null) {
                for (int i = 0; i < annotation.values.size(); i += 2) {
                    Object value = annotation.values.get(i + 1);
                    values.put((String) annotation.values.get(i), value instanceof String[] enumValue ? new EnumHolder(enumValue[1]) : value);
                }
            }
            result.add(new AnnotationData(Type.getType(annotation.desc), ElementType.FIELD, Type.getObjectType(owner), field, values));
        }
    }

    public record AnnotationData(Type annotationType, ElementType targetType, Type clazz, String memberName, Map<String, Object> annotationData) {
    }

    public record EnumHolder(String value) {
    }

}
