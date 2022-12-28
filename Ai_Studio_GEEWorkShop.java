//Çalışma Alanının Belirlenmesi

/////// 1. YOL ///////

/*
var images = ee.ImageCollection('COPERNICUS/S2_SR')
.filterBounds(cankaya)
.filterDate('2021-01-15','2021-07-15')
.filterMetadata('CLOUDY_PIXEL_PERCENTAGE','less_than',5)
.sort('CLOUDY_PIXEL_PERCENTAGE')
.mosaic();

//Map.centerObject(cankaya,13);

Map.addLayer(images,{bands:['B4','B3','B2'],max:7000},'Alan')

var img = ee.FeatureCollection(
  'FAO/GAUL_SIMPLIFIED_500m/2015/level2')
var cankaya = img.filter(
  ee.Filter.eq('ADM2_CODE', 27516))
Map.addLayer(cankaya, {color: 'yellow'}, 'Çankaya',false)
*/

var images = ee.ImageCollection('COPERNICUS/S2_SR')
.filterBounds(table)
.filterDate('2021-01-15','2021-07-15')
.filterMetadata('CLOUDY_PIXEL_PERCENTAGE','less_than',5)
.sort('CLOUDY_PIXEL_PERCENTAGE')
.mosaic();

Map.centerObject(table,13);

Map.addLayer(images,{bands:['B4','B3','B2'],max:7000},'Alan')



////////////////////////////////// FIELD CLIP ////////////////////////////////////////
var clip_image = images.clip(table);
Map.addLayer(clip_image,{bands:['B4','B3','B2'],max:7000},'Alan Clip')

////////////////////////////// IMAGE INFO ///////////////////////////////////////////
var info= table.getInfo();
print(info)




///////////////////////////// START CLASSIFICATION ///////////////////////////

// Merging of classes
var classNames = su.merge(yol).merge(bina).merge(yesilalan).merge(bosarazi);
print(classNames);

//Training sample regions
var bands = ['B4', 'B3', 'B2','B8'];
var training = clip_image.select(bands).sampleRegions({
  collection: classNames,
  properties: ['landcover'],
  scale: 30
});
print(training);

//////////////// RANDOM FOREST/////////////////
//Buradaki "7" parametresi karar ağacını belirtmektedir.
var classifier_RandomF = ee.Classifier.smileRandomForest(7).train({
  features: training,
  classProperty: 'landcover',
  inputProperties: bands
});


//Run the classification
var classified_RandomForest = images.select(bands).classify(classifier_RandomF);


//Display classification
Map.centerObject(classNames, 11);
Map.addLayer(classified_RandomForest,
{min: 0, max: 3, palette: ['blue','white', 'red','green','brown']},
'Classification Random Forest');
var accuracy_RF= classifier_RandomF.confusionMatrix();
print(accuracy_RF);
//'CE7E45','DF923D','F1B555','011D01','011301'
//'88E9F0', '063213','0F0632','F20911'
///////////// CART ///////////////

//Burada ilk parametre her ağaçtaki maksimum yaprak düğümü sayısı, ikinci parametre ise her bir yapraktaki popülasyon sayısı
var classifier_CART = ee.Classifier.smileCart(10, 3).train({
  features: training,
  classProperty: 'landcover',
  inputProperties: bands
});


//Run the classification
var classified_CART = images.select(bands).classify(classifier_CART);


//Display classification
Map.centerObject(classNames, 11);
Map.addLayer(classified_CART,
{min: 0, max: 3, palette: [ 'blue','white', 'red','green','brown']},
'Classification CART ');

var accuracy_RF= classifier_CART.confusionMatrix();
classified_CART.getInfo();