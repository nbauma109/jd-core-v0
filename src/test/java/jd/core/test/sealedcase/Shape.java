package jd.core.test.sealedcase;

public sealed interface Shape permits Circle, OpenShape, Branch, OpenType, Point {
}
