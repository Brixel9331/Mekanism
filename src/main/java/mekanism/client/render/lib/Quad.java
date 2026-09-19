package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.function.Consumer;
import mekanism.common.lib.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Quad {

    private final Vertex[] vertices;
    private Direction side;
    private TextureAtlasSprite sprite;
    private int tintIndex;
    private boolean shade;
    private boolean hasAmbientOcclusion;

    public Quad(TextureAtlasSprite sprite, Direction side, Vertex[] vertices) {
        this(sprite, side, vertices, -1, false, true);
    }

    public Quad(TextureAtlasSprite sprite, Direction side, Vertex[] vertices, int tintIndex, boolean shade, boolean hasAmbientOcclusion) {
        this.sprite = sprite;
        this.side = side;
        this.vertices = vertices;
        this.tintIndex = tintIndex;
        this.shade = shade;
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }

    public Quad(BakedQuad quad) {
        vertices = new Vertex[4];
        side = quad.getDirection();
        sprite = quad.getSprite();
        tintIndex = quad.getTintIndex();
        shade = quad.isShade();
        hasAmbientOcclusion = !(quad instanceof ModelQuad modelQuad) || modelQuad.hasAmbientOcclusion();
        int stride = DefaultVertexFormat.BLOCK.getIntegerSize();
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = Vertex.fromVanilla(quad.getVertices(), i * stride, side);
        }
    }

    public TextureAtlasSprite getTexture() {
        return sprite;
    }

    public void setTexture(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public int getTint() {
        return tintIndex;
    }

    public void setTint(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public void vertexTransform(Consumer<Vertex> transformation) {
        for (Vertex v : vertices) {
            transformation.accept(v);
        }
    }

    public boolean transform(QuadTransformation... transformations) {
        boolean transformed = false;
        for (QuadTransformation transform : transformations) {
            transformed |= transform.transform(this);
        }
        return transformed;
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public void setSide(Direction side) {
        this.side = side;
    }

    public Direction getSide() {
        return side;
    }

    public boolean isShade() {
        return shade;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public boolean hasAmbientOcclusion() {
        return hasAmbientOcclusion;
    }

    public void setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }

    public BakedQuad bake() {
        int stride = DefaultVertexFormat.BLOCK.getIntegerSize();
        int[] data = new int[vertices.length * stride];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i].writeVanilla(data, i * stride);
        }
        return new ModelQuad(data, tintIndex, side, sprite, shade, hasAmbientOcclusion);
    }

    public Quad copy() {
        Vertex[] newVertices = new Vertex[vertices.length];
        for (int i = 0; i < newVertices.length; i++) {
            newVertices[i] = vertices[i].copy(true);
        }
        return new Quad(sprite, side, newVertices, tintIndex, shade, hasAmbientOcclusion);
    }

    public Quad flip() {
        Vertex[] flipped = new Vertex[vertices.length];
        for (int i = 0; i < flipped.length; i++) {
            flipped[i] = vertices[i].flip();
        }
        return new Quad(sprite, side.getOpposite(), flipped, tintIndex, shade, hasAmbientOcclusion);
    }

    public static class Builder {

        private TextureAtlasSprite texture;
        private final Direction side;
        private Color color = Color.WHITE;

        private Vec3 vec1, vec2, vec3, vec4;

        private float minU, minV, maxU, maxV;
        private int lightU, lightV;

        private int tintIndex = -1;
        private boolean shade;
        private boolean hasAmbientOcclusion = true;
        private boolean contractUVs = true;

        public Builder(TextureAtlasSprite texture, Direction side) {
            this.texture = texture;
            this.side = side;
        }

        public Builder light(int light) {
            return light(LightTexture.block(light), LightTexture.sky(light));
        }

        public Builder light(int u, int v) {
            this.lightU = u;
            this.lightV = v;
            return this;
        }

        public Builder uv(float minU, float minV, float maxU, float maxV) {
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
            return this;
        }

        public Builder tex(TextureAtlasSprite texture) {
            this.texture = texture;
            return this;
        }

        public Builder tint(int tintIndex) {
            this.tintIndex = tintIndex;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder setShade(boolean shade) {
            this.shade = shade;
            return this;
        }

        public Builder setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
            this.hasAmbientOcclusion = hasAmbientOcclusion;
            return this;
        }

        public Builder contractUVs(boolean contractUVs) {
            this.contractUVs = contractUVs;
            return this;
        }

        public Builder pos(Vec3 tl, Vec3 bl, Vec3 br, Vec3 tr) {
            this.vec1 = tl;
            this.vec2 = bl;
            this.vec3 = br;
            this.vec4 = tr;
            return this;
        }

        public Builder rect(Vec3 start, double width, double height) {
            return rect(start, width, height, 1F / 16F); // default to 1/16 scale
        }

        // start = bottom left
        public Builder rect(Vec3 start, double width, double height, double scale) {
            start = start.scale(scale);
            Vec3 end;
            if (side.getAxis().isHorizontal()) {
                Vec3i normal = side.getNormal();
                end = start.add(normal.getZ() * width * scale, 0, normal.getX() * width * scale);
                if (side.getAxis() == Axis.X) {
                    //Wind vertices in a different order so that it faces the correct direction
                    return pos(start, start.add(0, height * scale, 0), end.add(0, height * scale, 0), end);
                }
            } else {
                end = start.add(width * scale, 0, 0);
            }
            return pos(start.add(0, height * scale, 0), start, end, end.add(0, height * scale, 0));
        }

        public Quad build() {
            Vertex[] vertices = new Vertex[4];
            Vector3f normal = vec3.subtract(vec2).cross(vec1.subtract(vec2)).normalize().toVector3f();
            vertices[0] = Vertex.create(vec1, normal, color, texture, minU, minV).light(lightU, lightV);
            vertices[1] = Vertex.create(vec2, normal, color, texture, minU, maxV).light(lightU, lightV);
            vertices[2] = Vertex.create(vec3, normal, color, texture, maxU, maxV).light(lightU, lightV);
            vertices[3] = Vertex.create(vec4, normal, color, texture, maxU, minV).light(lightU, lightV);
            Quad quad = new Quad(texture, side, vertices, tintIndex, shade, hasAmbientOcclusion);
            if (contractUVs) {
                QuadUtils.contractUVs(quad);
            }
            return quad;
        }
    }
}
