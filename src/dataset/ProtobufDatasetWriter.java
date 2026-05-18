package dataset;

import graph.Edge;
import graph.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ProtobufDatasetWriter {
    public void write(Dataset dataset, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.toAbsolutePath().getParent());
        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            outputStream.write(toByteArray(dataset));
        }
    }

    public byte[] toByteArray(Dataset dataset) {
        WireWriter writer = new WireWriter();
        for (GraphSample sample : dataset.getSamples()) {
            writer.writeMessage(1, encodeSample(sample));
        }
        return writer.toByteArray();
    }

    private byte[] encodeSample(GraphSample sample) {
        WireWriter writer = new WireWriter();
        for (Node node : sample.getGraph().getNodes()) {
            writer.writeMessage(1, encodeNode(node));
        }
        for (Edge edge : sample.getGraph().getEdges()) {
            writer.writeMessage(2, encodeEdge(edge));
        }
        writer.writeString(3, sample.getClassLabel());
        writer.writeDouble(4, sample.getNoiseLevel());
        return writer.toByteArray();
    }

    private byte[] encodeNode(Node node) {
        WireWriter writer = new WireWriter();
        writer.writeInt32(1, node.getId());
        writer.writeString(2, node.getLabel());
        return writer.toByteArray();
    }

    private byte[] encodeEdge(Edge edge) {
        WireWriter writer = new WireWriter();
        writer.writeInt32(1, edge.getSourceId());
        writer.writeInt32(2, edge.getTargetId());
        writer.writeString(3, edge.getLabel());
        return writer.toByteArray();
    }

    private static final class WireWriter {
        private final java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();

        void writeInt32(int fieldNumber, int value) {
            writeTag(fieldNumber, 0);
            writeVarint32(value);
        }

        void writeString(int fieldNumber, String value) {
            writeTag(fieldNumber, 2);
            byte[] bytes = value == null ? new byte[0] : value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            writeVarint32(bytes.length);
            writeBytes(bytes);
        }

        void writeDouble(int fieldNumber, double value) {
            writeTag(fieldNumber, 1);
            long bits = Double.doubleToRawLongBits(value);
            for (int shift = 0; shift < 64; shift += 8) {
                buffer.write((int) ((bits >> shift) & 0xFF));
            }
        }

        void writeMessage(int fieldNumber, byte[] payload) {
            writeTag(fieldNumber, 2);
            writeVarint32(payload.length);
            writeBytes(payload);
        }

        byte[] toByteArray() {
            return buffer.toByteArray();
        }

        private void writeTag(int fieldNumber, int wireType) {
            writeVarint32((fieldNumber << 3) | wireType);
        }

        private void writeVarint32(int value) {
            long unsigned = value & 0xFFFFFFFFL;
            while ((unsigned & ~0x7FL) != 0) {
                buffer.write((int) ((unsigned & 0x7F) | 0x80));
                unsigned >>>= 7;
            }
            buffer.write((int) unsigned);
        }

        private void writeBytes(byte[] bytes) {
            buffer.write(bytes, 0, bytes.length);
        }
    }
}