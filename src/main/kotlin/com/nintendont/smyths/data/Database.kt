package com.nintendont.smyths.data

import org.jetbrains.exposed.sql.*
import org.postgis.PGbox2d
import org.postgis.PGgeometry
import org.postgis.Point

object Products : Table() {
    var name = text("name")
    var price = decimal("price", 10, 2)
    //var category: Category = Category("", 0)
    // var productList: ProductList = ProductList("", 0)
    //var brand: Brand = Brand("")
    //var id = integer("id").autoIncrement().primaryKey()
}

object Categories : Table() {
        var name = text("name")
        var id = long("id").autoIncrement().primaryKey()
}

fun Table.point(name: String, srid: Int = 4326): Column<Point> = registerColumn(name, PointColumnType())

infix fun ExpressionWithColumnType<*>.within(box: PGbox2d) : Op<Boolean> = WithinOp(this, box)

private class PointColumnType(val srid: Int = 4326): ColumnType() {
    override fun sqlType() = "GEOMETRY(Point, $srid)"
    override fun valueFromDB(value: Any) = if (value is PGgeometry) value.geometry else value
    override fun notNullValueToDB(value: Any): Any {
        if (value is Point) {
            if (value.srid == Point.UNKNOWN_SRID) value.srid = srid
            return PGgeometry(value)
        }
        return value
    }
}

private class WithinOp(val expr1: Expression<*>, val box: PGbox2d) : Op<Boolean>() {
    override fun toSQL(queryBuilder: QueryBuilder) =
            "${expr1.toSQL(queryBuilder)} && ST_MakeEnvelope(${box.llb.x}, ${box.llb.y}, ${box.urt.x}, ${box.urt.y}, 4326)"
}