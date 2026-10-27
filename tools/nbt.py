"""Minimal NBT reader/writer (big-endian, gzip), no third-party deps.

Supports the tag types Minecraft structure templates use.  Written for
tools/gen_pet_stall.py, which has to emit structure templates that vanilla's
StructureTemplate loader accepts byte-for-byte.
"""

import gzip
import struct

TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12


class Tag:
    """A typed NBT value."""

    __slots__ = ("type", "value")

    def __init__(self, tag_type, value):
        self.type = tag_type
        self.value = value

    def __repr__(self):
        return f"Tag({self.type}, {self.value!r})"

    def __eq__(self, other):
        return isinstance(other, Tag) and self.type == other.type and self.value == other.value

    def __hash__(self):
        return hash((self.type, str(self.value)))


def Byte(v):
    return Tag(TAG_BYTE, int(v))


def Short(v):
    return Tag(TAG_SHORT, int(v))


def Int(v):
    return Tag(TAG_INT, int(v))


def Long(v):
    return Tag(TAG_LONG, int(v))


def Float(v):
    return Tag(TAG_FLOAT, float(v))


def Double(v):
    return Tag(TAG_DOUBLE, float(v))


def String(v):
    return Tag(TAG_STRING, str(v))


def ByteArray(v):
    return Tag(TAG_BYTE_ARRAY, bytes(v))


def IntArray(v):
    return Tag(TAG_INT_ARRAY, list(v))


def LongArray(v):
    return Tag(TAG_LONG_ARRAY, list(v))


def Compound(d=None):
    return Tag(TAG_COMPOUND, dict(d or {}))


def List(items, elem_type=None):
    """A TAG_List.  Minecraft needs the element type, so infer it when omitted."""
    items = list(items)
    if elem_type is None:
        elem_type = items[0].type if items else TAG_END
    for item in items:
        if item.type != elem_type:
            raise ValueError(f"list mixes tag types {elem_type} and {item.type}")
    return Tag(TAG_LIST, (elem_type, items))


# --------------------------------------------------------------------------- read


def _read_string(buf, pos):
    (length,) = struct.unpack_from(">H", buf, pos)
    pos += 2
    return buf[pos:pos + length].decode("utf-8"), pos + length


def _read_payload(buf, pos, tag_type):
    if tag_type == TAG_BYTE:
        (v,) = struct.unpack_from(">b", buf, pos)
        return Byte(v), pos + 1
    if tag_type == TAG_SHORT:
        (v,) = struct.unpack_from(">h", buf, pos)
        return Short(v), pos + 2
    if tag_type == TAG_INT:
        (v,) = struct.unpack_from(">i", buf, pos)
        return Int(v), pos + 4
    if tag_type == TAG_LONG:
        (v,) = struct.unpack_from(">q", buf, pos)
        return Long(v), pos + 8
    if tag_type == TAG_FLOAT:
        (v,) = struct.unpack_from(">f", buf, pos)
        return Float(v), pos + 4
    if tag_type == TAG_DOUBLE:
        (v,) = struct.unpack_from(">d", buf, pos)
        return Double(v), pos + 8
    if tag_type == TAG_BYTE_ARRAY:
        (length,) = struct.unpack_from(">i", buf, pos)
        pos += 4
        return ByteArray(buf[pos:pos + length]), pos + length
    if tag_type == TAG_STRING:
        value, pos = _read_string(buf, pos)
        return String(value), pos
    if tag_type == TAG_LIST:
        (elem_type, length) = struct.unpack_from(">bi", buf, pos)
        pos += 5
        items = []
        for _ in range(length):
            item, pos = _read_payload(buf, pos, elem_type)
            items.append(item)
        return List(items, elem_type), pos
    if tag_type == TAG_COMPOUND:
        out = {}
        while True:
            (child_type,) = struct.unpack_from(">b", buf, pos)
            pos += 1
            if child_type == TAG_END:
                return Compound(out), pos
            name, pos = _read_string(buf, pos)
            out[name], pos = _read_payload(buf, pos, child_type)
    if tag_type == TAG_INT_ARRAY:
        (length,) = struct.unpack_from(">i", buf, pos)
        pos += 4
        vals = list(struct.unpack_from(f">{length}i", buf, pos))
        return IntArray(vals), pos + 4 * length
    if tag_type == TAG_LONG_ARRAY:
        (length,) = struct.unpack_from(">i", buf, pos)
        pos += 4
        vals = list(struct.unpack_from(f">{length}q", buf, pos))
        return LongArray(vals), pos + 8 * length
    raise ValueError(f"unknown tag type {tag_type} at {pos}")


def load_bytes(raw):
    """Parse gzip-compressed NBT bytes into a root Compound."""
    buf = gzip.decompress(raw)
    (root_type,) = struct.unpack_from(">b", buf, 0)
    if root_type != TAG_COMPOUND:
        raise ValueError(f"root tag is {root_type}, expected compound")
    name, pos = _read_string(buf, 1)
    root, _ = _read_payload(buf, pos, TAG_COMPOUND)
    return name, root


def load(path):
    with open(path, "rb") as fh:
        return load_bytes(fh.read())


# --------------------------------------------------------------------------- write


def _write_string(out, s):
    raw = s.encode("utf-8")
    out += struct.pack(">H", len(raw))
    out += raw


def _write_payload(out, tag):
    t = tag.type
    if t == TAG_BYTE:
        out += struct.pack(">b", tag.value)
    elif t == TAG_SHORT:
        out += struct.pack(">h", tag.value)
    elif t == TAG_INT:
        out += struct.pack(">i", tag.value)
    elif t == TAG_LONG:
        out += struct.pack(">q", tag.value)
    elif t == TAG_FLOAT:
        out += struct.pack(">f", tag.value)
    elif t == TAG_DOUBLE:
        out += struct.pack(">d", tag.value)
    elif t == TAG_BYTE_ARRAY:
        out += struct.pack(">i", len(tag.value))
        out += bytes(tag.value)
    elif t == TAG_STRING:
        _write_string(out, tag.value)
    elif t == TAG_LIST:
        elem_type, items = tag.value
        out += struct.pack(">bi", elem_type, len(items))
        for item in items:
            _write_payload(out, item)
    elif t == TAG_COMPOUND:
        for name, child in tag.value.items():
            out += struct.pack(">b", child.type)
            _write_string(out, name)
            _write_payload(out, child)
        out += struct.pack(">b", TAG_END)
    elif t == TAG_INT_ARRAY:
        out += struct.pack(">i", len(tag.value))
        out += struct.pack(f">{len(tag.value)}i", *tag.value)
    elif t == TAG_LONG_ARRAY:
        out += struct.pack(">i", len(tag.value))
        out += struct.pack(f">{len(tag.value)}q", *tag.value)
    else:
        raise ValueError(f"cannot write tag type {t}")


def dump_bytes(root, name=""):
    out = bytearray()
    out += struct.pack(">b", TAG_COMPOUND)
    _write_string(out, name)
    _write_payload(out, root)
    return gzip.compress(bytes(out), mtime=0)


def dump(path, root, name=""):
    with open(path, "wb") as fh:
        fh.write(dump_bytes(root, name))
