-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: bd_amaya
-- ------------------------------------------------------
-- Server version	8.4.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `almacenes`
--

DROP TABLE IF EXISTS `almacenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `almacenes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `direccion` varchar(200) DEFAULT NULL,
  `estado` int NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4ilm0f1weyncwuyyikn7rrlf4` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `almacenes`
--

LOCK TABLES `almacenes` WRITE;
/*!40000 ALTER TABLE `almacenes` DISABLE KEYS */;
INSERT INTO `almacenes` VALUES (1,'Primer piso de la vivienda familiar',1,'2026-09-22 04:44:15.761267','Almacén Casa'),(2,'Mercado Unicachi',1,'2026-09-22 04:44:15.779127','Almacén Unicachi'),(3,'Prueba',0,'2026-09-22 04:47:53.310511','Almacén Prueba');
/*!40000 ALTER TABLE `almacenes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado` int NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqcog8b7hps1hioi9onqwjdt6y` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
INSERT INTO `categorias` VALUES (1,1,'2026-09-22 00:18:48.685735','Bebidas'),(2,1,'2026-09-22 00:19:59.607360','Prueba1'),(3,1,'2026-09-22 00:20:07.112644','Prueba2'),(4,1,'2026-09-22 01:05:32.638001','Limpieza'),(5,0,'2026-10-06 13:26:47.765550','Prueba3');
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apellido_materno` varchar(100) DEFAULT NULL,
  `apellido_paterno` varchar(100) DEFAULT NULL,
  `direccion` varchar(250) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `estado` int NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `nombre_comercial` varchar(200) DEFAULT NULL,
  `nombres` varchar(100) DEFAULT NULL,
  `numero_documento` varchar(11) NOT NULL,
  `razon_social` varchar(200) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `tipo_documento` varchar(10) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2viccgf178bd74vfbq8ctsv8t` (`numero_documento`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES (1,'Gonzales','Hurtado',NULL,NULL,1,'2026-10-22 00:52:45.594297',NULL,'Alex','98645128',NULL,NULL,'DNI');
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comprobantes`
--

DROP TABLE IF EXISTS `comprobantes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comprobantes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado` int NOT NULL,
  `fecha_emision` date NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `numero` varchar(30) NOT NULL,
  `observacion` varchar(300) DEFAULT NULL,
  `tipo` enum('COMPRA','VENTA') NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `id_proveedor` bigint DEFAULT NULL,
  `id_usuario` bigint DEFAULT NULL,
  `id_cliente` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7p5mu82mvi8o7blccmobd4y0d` (`id_proveedor`),
  KEY `FKlob9n64lmmlerkyhwumfsdusr` (`id_usuario`),
  KEY `FKhxyxpowm0fhe5voncckgtfipd` (`id_cliente`),
  CONSTRAINT `FK7p5mu82mvi8o7blccmobd4y0d` FOREIGN KEY (`id_proveedor`) REFERENCES `proveedores` (`id`),
  CONSTRAINT `FKhxyxpowm0fhe5voncckgtfipd` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id`),
  CONSTRAINT `FKlob9n64lmmlerkyhwumfsdusr` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comprobantes`
--

LOCK TABLES `comprobantes` WRITE;
/*!40000 ALTER TABLE `comprobantes` DISABLE KEYS */;
INSERT INTO `comprobantes` VALUES (1,0,'2026-09-23','2026-09-23 01:29:45.426692','F00-0001',NULL,'COMPRA',1152.00,1,1,NULL),(2,1,'2026-09-23','2026-09-23 02:08:29.554063','F00-03',NULL,'COMPRA',42.00,1,1,NULL),(3,1,'2026-09-23','2026-09-23 02:09:59.442595','F00-00003',NULL,'VENTA',45.00,NULL,1,NULL),(4,1,'2026-09-23','2026-09-23 02:18:30.252892','F003-33',NULL,'COMPRA',35.00,1,1,NULL),(8,0,'2026-10-10','2026-10-10 15:09:33.447531','20100070970',NULL,'COMPRA',14.00,1,1,NULL),(9,1,'2026-10-19','2026-10-20 00:27:00.622792','F001-00000004',NULL,'COMPRA',45.00,1,1,NULL),(10,0,'2026-10-22','2026-10-22 17:51:50.685051','B001-91502433',NULL,'VENTA',5.00,NULL,1,1),(11,0,'2026-10-22','2026-10-22 18:06:10.525767','B001-92363059',NULL,'VENTA',5.00,NULL,1,1),(12,1,'2026-10-22','2026-10-22 18:10:03.589529','B001-92592933',NULL,'VENTA',5.00,NULL,1,1),(13,0,'2026-10-22','2026-10-23 04:14:15.932880','B001-28843789',NULL,'VENTA',5.00,NULL,1,1),(14,1,'2026-10-22','2026-10-23 04:24:02.931544','B001-29431835',NULL,'VENTA',5.00,NULL,1,1),(15,1,'2026-10-23','2026-10-23 22:03:48.558223','B001-00000003',NULL,'VENTA',5.00,NULL,1,1);
/*!40000 ALTER TABLE `comprobantes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_comprobante`
--

DROP TABLE IF EXISTS `detalle_comprobante`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_comprobante` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` decimal(12,2) NOT NULL,
  `precio_unitario` decimal(12,2) NOT NULL,
  `id_almacen` bigint NOT NULL,
  `id_comprobante` bigint NOT NULL,
  `id_producto` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg6221f8xbewomebkajf8g1y9s` (`id_almacen`),
  KEY `FKfk6su5acbejg99h1t8ch5pwsv` (`id_comprobante`),
  KEY `FKa4bc71ugrrpio6ce32mhlgoc` (`id_producto`),
  CONSTRAINT `FKa4bc71ugrrpio6ce32mhlgoc` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id`),
  CONSTRAINT `FKfk6su5acbejg99h1t8ch5pwsv` FOREIGN KEY (`id_comprobante`) REFERENCES `comprobantes` (`id`),
  CONSTRAINT `FKg6221f8xbewomebkajf8g1y9s` FOREIGN KEY (`id_almacen`) REFERENCES `almacenes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_comprobante`
--

LOCK TABLES `detalle_comprobante` WRITE;
/*!40000 ALTER TABLE `detalle_comprobante` DISABLE KEYS */;
INSERT INTO `detalle_comprobante` VALUES (1,12.00,96.00,2,1,1),(2,6.00,7.00,1,2,2),(3,5.00,9.00,1,3,2),(4,5.00,7.00,1,4,2),(11,1.00,7.00,1,8,2),(12,1.00,7.00,1,8,2),(13,10.00,4.50,1,9,9),(14,1.00,5.00,1,10,9),(15,1.00,5.00,1,11,9),(16,1.00,5.00,1,12,9),(17,1.00,5.00,1,13,9),(18,1.00,5.00,1,14,9),(19,1.00,5.00,1,15,9);
/*!40000 ALTER TABLE `detalle_comprobante` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos`
--

DROP TABLE IF EXISTS `movimientos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` decimal(12,2) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `observacion` varchar(300) DEFAULT NULL,
  `stock_anterior` decimal(12,2) NOT NULL,
  `stock_nuevo` decimal(12,2) NOT NULL,
  `tipo` enum('ENTRADA','SALIDA') NOT NULL,
  `id_almacen` bigint NOT NULL,
  `id_producto` bigint NOT NULL,
  `id_usuario` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKar2uwtha69ijkieu78f3yc5p4` (`id_almacen`),
  KEY `FKq7gb4wrm2hx3qiwh2q9vll7l2` (`id_producto`),
  KEY `FK2vy4x99hlkxynsue71d15e4ji` (`id_usuario`),
  CONSTRAINT `FK2vy4x99hlkxynsue71d15e4ji` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKar2uwtha69ijkieu78f3yc5p4` FOREIGN KEY (`id_almacen`) REFERENCES `almacenes` (`id`),
  CONSTRAINT `FKq7gb4wrm2hx3qiwh2q9vll7l2` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos`
--

LOCK TABLES `movimientos` WRITE;
/*!40000 ALTER TABLE `movimientos` DISABLE KEYS */;
INSERT INTO `movimientos` VALUES (1,12.00,'2026-09-23 01:29:45.459695','COMPRA F00-0001',0.00,12.00,'ENTRADA',2,1,1),(2,12.00,'2026-09-23 02:02:02.742856','Anulación F00-0001',12.00,0.00,'SALIDA',2,1,1),(3,6.00,'2026-09-23 02:08:29.576062','COMPRA F00-03',0.00,6.00,'ENTRADA',1,2,1),(4,5.00,'2026-09-23 02:09:59.450600','VENTA F00-00003',6.00,1.00,'SALIDA',1,2,1),(5,5.00,'2026-09-23 02:18:30.299894','COMPRA F003-33',1.00,6.00,'ENTRADA',1,2,1),(12,1.00,'2026-10-10 15:09:33.507531','COMPRA 20100070970',6.00,7.00,'ENTRADA',1,2,1),(13,1.00,'2026-10-10 15:09:33.520536','COMPRA 20100070970',7.00,8.00,'ENTRADA',1,2,1),(14,1.00,'2026-10-10 15:09:50.064643','Anulación 20100070970',8.00,7.00,'SALIDA',1,2,1),(15,1.00,'2026-10-10 15:09:50.071644','Anulación 20100070970',7.00,6.00,'SALIDA',1,2,1),(16,10.00,'2026-10-20 00:27:00.618842','COMPRA F001-00000004',0.00,10.00,'ENTRADA',1,9,1),(17,1.00,'2026-10-22 17:51:50.632606','VENTA B001-91502433',10.00,9.00,'SALIDA',1,9,1),(18,1.00,'2026-10-22 18:04:02.332204','Anulación B001-91502433',9.00,10.00,'ENTRADA',1,9,1),(19,1.00,'2026-10-22 18:06:10.524763','VENTA B001-92363059',10.00,9.00,'SALIDA',1,9,1),(20,1.00,'2026-10-22 18:06:21.659105','Anulación B001-92363059',9.00,10.00,'ENTRADA',1,9,1),(21,1.00,'2026-10-22 18:10:03.588529','VENTA B001-92592933',10.00,9.00,'SALIDA',1,9,1),(22,1.00,'2026-10-23 04:14:15.909355','VENTA B001-28843789',9.00,8.00,'SALIDA',1,9,1),(23,1.00,'2026-10-23 04:15:22.758561','Anulación B001-28843789',8.00,9.00,'ENTRADA',1,9,1),(24,1.00,'2026-10-23 04:24:02.929543','VENTA B001-29431835',9.00,8.00,'SALIDA',1,9,1),(25,1.00,'2026-10-23 22:03:48.510678','VENTA B001-00000003',8.00,7.00,'SALIDA',1,9,1);
/*!40000 ALTER TABLE `movimientos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opciones`
--

DROP TABLE IF EXISTS `opciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opciones` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(50) NOT NULL,
  `estado` int NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `orden` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKesdmercu340d6d5ccoua1xejm` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opciones`
--

LOCK TABLES `opciones` WRITE;
/*!40000 ALTER TABLE `opciones` DISABLE KEYS */;
INSERT INTO `opciones` VALUES (1,'inicio',1,'Inicio',1),(2,'proveedores',1,'Proveedores',2),(3,'categorias',1,'Categorías',3),(4,'comprobantes',1,'Comprobantes',4),(6,'productos',1,'Productos',4),(7,'almacenes',1,'Almacenes',5),(8,'compras',1,'Compras',6),(9,'movimientos',1,'Movimientos',6),(10,'clientes',1,'Clientes',8),(11,'usuarios',1,'Usuarios',9),(12,'kardex',1,'Kardex',10);
/*!40000 ALTER TABLE `opciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(20) NOT NULL,
  `descripcion` varchar(300) DEFAULT NULL,
  `estado` int NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `unidad_medida` varchar(20) NOT NULL,
  `id_categoria` bigint NOT NULL,
  `precio_compra` decimal(12,2) NOT NULL,
  `precio_venta` decimal(12,2) NOT NULL,
  `id_almacen_principal` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKh04wpyqwddobltuqq56cp6s05` (`codigo`),
  UNIQUE KEY `UKmlgw7js72hh2xtd4mvpdqfsbe` (`nombre`),
  KEY `FKdtoa37luoxhhvbicrfiu5ygbj` (`id_categoria`),
  KEY `FKg04963bo2al2wjpmp7ecldtp` (`id_almacen_principal`),
  CONSTRAINT `FKdtoa37luoxhhvbicrfiu5ygbj` FOREIGN KEY (`id_categoria`) REFERENCES `categorias` (`id`),
  CONSTRAINT `FKg04963bo2al2wjpmp7ecldtp` FOREIGN KEY (`id_almacen_principal`) REFERENCES `almacenes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,'PRD-00001','Gaseosa',0,'2026-09-22 05:21:20.349103','Coca Cola 2L','UNIDAD',1,0.00,0.00,NULL),(2,'PRD-00002',NULL,1,'2026-09-23 02:07:34.372403','Inca Kola 2.5L','UNIDAD',1,7.00,9.00,NULL),(9,'PRD-00003',NULL,1,'2026-10-10 16:17:11.198045','Ariel','UNIDAD',4,4.50,5.00,1);
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedores`
--

DROP TABLE IF EXISTS `proveedores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `direccion` varchar(200) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `estado` int NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `nombre_comercial` varchar(150) DEFAULT NULL,
  `razon_social` varchar(150) NOT NULL,
  `ruc` varchar(11) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKl2qkpg6gem3abown28yrhqjph` (`ruc`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedores`
--

LOCK TABLES `proveedores` WRITE;
/*!40000 ALTER TABLE `proveedores` DISABLE KEYS */;
INSERT INTO `proveedores` VALUES (1,NULL,NULL,1,'2026-09-22 02:07:48.470923',NULL,'CORPORACION SERCOPLUS S.A.C.','20518679121',NULL);
/*!40000 ALTER TABLE `proveedores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol_opcion`
--

DROP TABLE IF EXISTS `rol_opcion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol_opcion` (
  `id_opcion` bigint NOT NULL,
  `id_rol` bigint NOT NULL,
  PRIMARY KEY (`id_opcion`,`id_rol`),
  KEY `FKm66jgua4bnx6uj134jqhi9ofs` (`id_rol`),
  CONSTRAINT `FK3vw5yu84vgi8a58wekvm8w0mj` FOREIGN KEY (`id_opcion`) REFERENCES `opciones` (`id`),
  CONSTRAINT `FKm66jgua4bnx6uj134jqhi9ofs` FOREIGN KEY (`id_rol`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_opcion`
--

LOCK TABLES `rol_opcion` WRITE;
/*!40000 ALTER TABLE `rol_opcion` DISABLE KEYS */;
INSERT INTO `rol_opcion` VALUES (1,1),(2,1),(3,1),(4,1),(6,1),(7,1),(9,1),(10,1),(11,1),(12,1),(1,2),(2,2),(3,2),(6,2),(7,2),(9,2),(12,2),(1,3),(4,3),(6,3),(10,3);
/*!40000 ALTER TABLE `rol_opcion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(150) DEFAULT NULL,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKldv0v52e0udsh2h1rs0r0gw1n` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'Administrador del sistema','ADMINISTRADOR'),(2,'Gestión de almacenes e inventario','ALMACENERO'),(3,'Gestión de clientes y ventas','VENDEDOR');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock`
--

DROP TABLE IF EXISTS `stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` decimal(12,2) NOT NULL,
  `fecha_actualizacion` datetime(6) NOT NULL,
  `id_almacen` bigint NOT NULL,
  `id_producto` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKftpudaq9kdqnparcuklb4gae6` (`id_producto`,`id_almacen`),
  KEY `FKrhqac3nwyfkslaqwecobrxo68` (`id_almacen`),
  CONSTRAINT `FKaehulqq7ntq51cnm17hmas1bc` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id`),
  CONSTRAINT `FKrhqac3nwyfkslaqwecobrxo68` FOREIGN KEY (`id_almacen`) REFERENCES `almacenes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock`
--

LOCK TABLES `stock` WRITE;
/*!40000 ALTER TABLE `stock` DISABLE KEYS */;
INSERT INTO `stock` VALUES (1,0.00,'2026-09-22 05:21:20.368127',1,1),(2,0.00,'2026-09-23 02:02:02.756855',2,1),(3,6.00,'2026-10-10 15:09:50.076638',1,2),(10,0.00,'2026-10-10 16:17:11.231036',2,9),(11,7.00,'2026-10-23 22:03:48.571248',1,9);
/*!40000 ALTER TABLE `stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(150) DEFAULT NULL,
  `estado` int NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `nombre_empleado` varchar(100) NOT NULL,
  `nombre_usuario` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `id_rol` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKof5vabgukahdwmgxk4kjrbu98` (`nombre_usuario`),
  UNIQUE KEY `UKkfsp0s1tflm1cwlj8idhqsad0` (`email`),
  KEY `FK3kl77pehgupicftwfreqnjkll` (`id_rol`),
  CONSTRAINT `FK3kl77pehgupicftwfreqnjkll` FOREIGN KEY (`id_rol`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'admin@amaya.com',1,'2026-09-21 21:48:46.215255','Administrador Amaya','admin','$2a$10$JT0/dz6Afrf6GfRCrzWVVOaKOlOC4pvTen/ADi7.qXemMrDhiEFji',1),(2,NULL,1,'2026-10-19 23:59:06.563255','prueba1','prueba1','$2a$10$/EuoLv5NZ2pUJzT2CdnzMOVhjMzbJC/Kagh6oHMwIX2gjt1/555xy',2),(3,NULL,1,'2026-10-24 06:40:06.568024','angel','angel','$2a$10$6E9l1bRcM.IULBK1ngTpH.x6ukQBQnXolCtFeRMO62p9bcz6l1dim',3);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-24  2:16:57
