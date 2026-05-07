-- =============================================
-- TECHSTORE PRO - SEED DATA
-- Nuevas categorias y productos tecnologicos
-- =============================================

-- NUEVAS CATEGORIAS
INSERT INTO categories (name, active, created_at) VALUES
  ('Laptops',        true, NOW()),
  ('Monitores',      true, NOW()),
  ('Smartphones',    true, NOW()),
  ('Tablets',        true, NOW()),
  ('Almacenamiento', true, NOW()),
  ('Componentes',    true, NOW()),
  ('Camaras',        true, NOW()),
  ('Impresoras',     true, NOW())
ON CONFLICT (name) DO NOTHING;

-- =============================================
-- LAPTOPS
-- =============================================
INSERT INTO products (name, category, category_id, description, image_url, price, stock, active, created_at) VALUES
(
  'MacBook Air M3 13"',
  'Laptops',
  (SELECT id FROM categories WHERE name = 'Laptops'),
  'Laptop ultradelgada Apple con chip M3, pantalla Liquid Retina 13.6", 8GB RAM unificada, 256GB SSD. Bateria hasta 18 horas. Diseno en aluminio reciclado.',
  'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/macbook-air-midnight-config-20220606?wid=820&hei=498&fmt=jpeg&qlt=90',
  1249.99, 15, true, NOW()
),
(
  'ASUS ROG Zephyrus G14',
  'Laptops',
  (SELECT id FROM categories WHERE name = 'Laptops'),
  'Laptop gaming con AMD Ryzen 9 8945HS, RTX 4060 8GB, pantalla OLED 14" 120Hz, 16GB DDR5, 1TB NVMe SSD. Chasis AniMe Matrix LED.',
  'https://dlcdnwebimgs.asus.com/gain/A9C5E7B4-98BF-4B99-8AC1-2CA6F94E8D57/w1000/h732',
  1599.99, 8, true, NOW()
),
(
  'Dell XPS 15 OLED',
  'Laptops',
  (SELECT id FROM categories WHERE name = 'Laptops'),
  'Laptop premium con Intel Core Ultra 9, NVIDIA RTX 4070, pantalla OLED 15.6" 3.5K touch, 32GB RAM, 1TB SSD. Diseno CNC en aluminio y fibra de carbono.',
  'https://i.dell.com/is/image/DellContent/content/dam/ss2/product-images/dell-client-products/notebooks/xps-notebooks/xps-15-9530/media-gallery/black/notebook-xps-15-9530-t-black-gallery-4.psd?fmt=png-alpha&pscan=auto&scl=1&hei=402&wid=402&qlt=100',
  1899.99, 6, true, NOW()
),
(
  'Lenovo ThinkPad X1 Carbon Gen 12',
  'Laptops',
  (SELECT id FROM categories WHERE name = 'Laptops'),
  'Business laptop ultraligero 1.12kg con Intel Core Ultra 7, pantalla 14" IPS 2.8K, 32GB LPDDR5, 1TB SSD, teclado retroiluminado y lector de huellas.',
  'https://p1-ofp.static.pub/fes/cms/2024/01/05/1owmq4k3p22fzjrxk2i5r5a0p8mmey765527.png',
  1749.99, 10, true, NOW()
),

-- =============================================
-- MONITORES
-- =============================================
(
  'LG UltraGear 27GP950-B 4K',
  'Monitores',
  (SELECT id FROM categories WHERE name = 'Monitores'),
  'Monitor gaming 4K UHD Nano IPS, 144Hz, 1ms GTG, HDMI 2.1, compatible G-Sync y FreeSync Premium Pro. Ideal para PS5 y Xbox Series X.',
  'https://www.lg.com/us/images/monitors/md08003231/gallery/medium01.jpg',
  699.99, 20, true, NOW()
),
(
  'Samsung Odyssey G9 49"',
  'Monitores',
  (SELECT id FROM categories WHERE name = 'Monitores'),
  'Monitor curvo ultra-wide 49" DQHD 5120x1440 QLED, 240Hz, 1ms, DisplayHDR 1000. Curvatura 1000R para experiencia inmersiva total.',
  'https://image-us.samsung.com/SamsungUS/home/computing/monitors/gaming/all-monitors/07112022/G9_Front_Black_1600x1200.jpg',
  1299.99, 5, true, NOW()
),
(
  'ASUS ProArt PA32UCX 32"',
  'Monitores',
  (SELECT id FROM categories WHERE name = 'Monitores'),
  'Monitor profesional 4K IPS 32", 120Hz, Dolby Vision, DCI-P3 99%, HDR True Black 400. Para disenadores graficos y video profesional.',
  'https://dlcdnwebimgs.asus.com/gain/c6f2f53a-fdd2-42c0-b6d3-4cbce39b3a30/w1000/h732',
  2499.99, 4, true, NOW()
),

-- =============================================
-- SMARTPHONES
-- =============================================
(
  'iPhone 16 Pro Max 256GB',
  'Smartphones',
  (SELECT id FROM categories WHERE name = 'Smartphones'),
  'Smartphone Apple con chip A18 Pro, camara 48MP triple, pantalla Super Retina XDR 6.9" ProMotion 120Hz, titanio grado aeroespacial.',
  'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-16-pro-finish-select-202409-6-9inch-deserttitanium?wid=800&hei=800&fmt=jpeg&qlt=90',
  1199.99, 25, true, NOW()
),
(
  'Samsung Galaxy S25 Ultra 512GB',
  'Smartphones',
  (SELECT id FROM categories WHERE name = 'Smartphones'),
  'Flagship Samsung con Snapdragon 8 Elite, camara 200MP con zoom optico 10x, pantalla Dynamic AMOLED 6.9" 120Hz, S Pen integrado, 12GB RAM.',
  'https://images.samsung.com/is/image/samsung/p6pim/latin/2501/gallery/latin-galaxy-s25-ultra-s938-sm-s938bzakeub-thumb-543537793?$650_519_PNG$',
  1249.99, 18, true, NOW()
),
(
  'Google Pixel 9 Pro XL',
  'Smartphones',
  (SELECT id FROM categories WHERE name = 'Smartphones'),
  'Smartphone Google con chip Tensor G4, camara Triple 50MP+48MP+48MP, pantalla LTPO OLED 6.8" 120Hz, 16GB RAM, 7 anos de actualizaciones.',
  'https://lh3.googleusercontent.com/j0NpQEiM3oINMJC4mDIv7iqKB1LV5m0F6UxDkJHUqnlBxBnPrCJpb3mCVMYRHtpEXjHrEhkqSXqfH6A=rw-e365-w600',
  999.99, 12, true, NOW()
),

-- =============================================
-- TABLETS
-- =============================================
(
  'iPad Pro M4 13" WiFi 256GB',
  'Tablets',
  (SELECT id FROM categories WHERE name = 'Tablets'),
  'Tablet Apple con chip M4, pantalla Ultra Retina XDR OLED 13", 8GB RAM, compatible Apple Pencil Pro y Magic Keyboard. El mas delgado de Apple.',
  'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-model-select-gallery-2-202405?wid=800&hei=800&fmt=jpeg&qlt=95',
  1099.99, 10, true, NOW()
),
(
  'Samsung Galaxy Tab S10 Ultra',
  'Tablets',
  (SELECT id FROM categories WHERE name = 'Tablets'),
  'Tablet premium 14.6" Dynamic AMOLED 2X, Snapdragon 8 Gen 3, 12GB RAM, 256GB, S Pen incluido, camara doble frontal para videoconferencias.',
  'https://images.samsung.com/is/image/samsung/p6pim/latin/2408/gallery/latin-galaxy-tab-s10-ultra-x910-sm-x910nzaaeub-thumb-543537793?$650_519_PNG$',
  999.99, 7, true, NOW()
),

-- =============================================
-- ALMACENAMIENTO
-- =============================================
(
  'Samsung 990 Pro NVMe 2TB',
  'Almacenamiento',
  (SELECT id FROM categories WHERE name = 'Almacenamiento'),
  'SSD NVMe PCIe 4.0 con velocidades hasta 7450 MB/s lectura y 6900 MB/s escritura. Ideal para gaming, edicion de video 4K y workstations.',
  'https://images.samsung.com/is/image/samsung/p6pim/latin/mz-v9p2t0b/gallery/latin-990-pro-mz-v9p2t0b-536681979?$650_519_PNG$',
  189.99, 35, true, NOW()
),
(
  'WD Black SN850X 1TB NVMe',
  'Almacenamiento',
  (SELECT id FROM categories WHERE name = 'Almacenamiento'),
  'SSD NVMe M.2 PCIe Gen4 optimizado para gaming con velocidades 7300 MB/s lectura. Game Mode 2.0 para carga predictiva. Compatible PS5.',
  'https://shop.westerndigital.com/content/dam/store/en-us/assets/products/internal-storage/wd-black-sn850x-nvme-ssd/gallery/wd-black-sn850x-nvme-ssd-1tb-main.png.thumb.1280.1280.png',
  129.99, 40, true, NOW()
),
(
  'Seagate IronWolf Pro 8TB NAS',
  'Almacenamiento',
  (SELECT id FROM categories WHERE name = 'Almacenamiento'),
  'Disco duro 3.5" 7200 RPM optimizado para servidores NAS 24x7. 256MB cache, velocidad 260MB/s, con recuperacion de datos IronWolf Health.',
  'https://www.seagate.com/content/dam/seagate/migrated-assets/www-content/product-content/ironwolf/en-us/gallery/ironwolf-pro-front-3-4-right-400x400.png',
  249.99, 22, true, NOW()
),

-- =============================================
-- COMPONENTES
-- =============================================
(
  'NVIDIA GeForce RTX 5080 16GB',
  'Componentes',
  (SELECT id FROM categories WHERE name = 'Componentes'),
  'GPU Blackwell con 16GB GDDR7, DLSS 4 con Multi Frame Generation, ray tracing de 3era generacion, TDP 360W. Rendimiento 4K ultra para gaming.',
  'https://assets.nvidia.com/en-us/geforce/news/rtx-50-series/rtx-5080-hero.png',
  999.99, 6, true, NOW()
),
(
  'AMD Ryzen 9 9950X',
  'Componentes',
  (SELECT id FROM categories WHERE name = 'Componentes'),
  'CPU desktop 16 nucleos / 32 hilos, hasta 5.7GHz boost, arquitectura Zen 5, cache L3 64MB, TDP 170W. Zocalo AM5. El mas rapido de AMD para workstations.',
  'https://www.amd.com/content/dam/amd/en/images/products/desktop-processors/9000-series/2555028-ryzen-9-9950x-PIB.png',
  649.99, 9, true, NOW()
),
(
  'Corsair Dominator Titanium 32GB DDR5 6000MHz',
  'Componentes',
  (SELECT id FROM categories WHERE name = 'Componentes'),
  'Kit 2x16GB DDR5 6000MHz CL30, Intel XMP 3.0 y AMD EXPO, iluminacion ARGB iCUE, disipadores de aluminio mecanizado CNC. Para Z790 y X670E.',
  'https://www.corsair.com/medias/sys_master/images/images/h48/hff/9463832051742/CMP32GX5M2B6000C30-Gallery-DTi-Black-01.png',
  179.99, 28, true, NOW()
),

-- =============================================
-- CAMARAS
-- =============================================
(
  'Sony Alpha 7 IV Mirrorless',
  'Camaras',
  (SELECT id FROM categories WHERE name = 'Camaras'),
  'Camara mirrorless full-frame 33MP, video 4K 60fps sin crop, estabilizacion optica 5.5 pasos, AF en tiempo real con IA, 828 puntos de fase.',
  'https://www.sony.com/image/5d02da5df552836db894cead8a68f5f3?fmt=png-alpha&wid=440',
  2499.99, 5, true, NOW()
),
(
  'Canon EOS R6 Mark II',
  'Camaras',
  (SELECT id FROM categories WHERE name = 'Camaras'),
  'Camara mirrorless 24.2MP full-frame, video 6K RAW interno, rafaga 40fps, IBIS 8 pasos, AF Dual Pixel CMOS II con seguimiento de sujetos en movimiento.',
  'https://store.canon.com.au/dw/image/v2/BBGC_PRD/on/demandware.static/-/Sites-master-catalog/default/dw66b78cf2/images/large/5811C012AA.jpg',
  2799.99, 4, true, NOW()
),

-- =============================================
-- IMPRESORAS
-- =============================================
(
  'HP LaserJet Pro MFP M428fdw',
  'Impresoras',
  (SELECT id FROM categories WHERE name = 'Impresoras'),
  'Impresora multifuncion laser monocromo: imprime, escanea, copia y fax. 38ppm, duplex automatico, WiFi, Ethernet, pantalla tactil 2.7". Oficinas medianas.',
  'https://ssl-product-images.www8-hp.com/digmedialib/prodimg/lowres/c07851745.png',
  349.99, 14, true, NOW()
),
(
  'Epson EcoTank ET-16650 A3+',
  'Impresoras',
  (SELECT id FROM categories WHERE name = 'Impresoras'),
  'Impresora multifuncion A3+ con sistema de tanque recargable EcoTank. Sin cartuchos, imprime hasta 14000 paginas color. WiFi, Ethernet, duplex automatico.',
  'https://epson.com/wcsstore/EpsonStorefront/ep.storefront/pub/media/catalog/product/6/6/66d5fdef93b01e08cd1bd3dbb8a3edac.png',
  499.99, 8, true, NOW()
);

-- =============================================
-- VERIFICACION FINAL
-- =============================================
SELECT c.name AS categoria, COUNT(p.id) AS total_productos
FROM categories c
LEFT JOIN products p ON p.category_id = c.id
GROUP BY c.name
ORDER BY c.name;
