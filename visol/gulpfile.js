'use strict';

const gulp = require('gulp');
const sass = require('gulp-sass')(require('sass'));
const del = require('del');
const pathToApp = './src/main/webapp';

gulp.task('styles', () => {
  return gulp.src(pathToApp + '/styles/index.sass')
      .pipe(sass().on('error', sass.logError))
      .pipe(gulp.dest(pathToApp + '/'));
});

gulp.task('bootstrap', () => {
  return gulp.src(pathToApp + '/styles/bootstrap.sass')
      .pipe(sass().on('error', sass.logError))
      .pipe(gulp.dest(pathToApp + '/'));
});

gulp.task('watch', () => {
  gulp.watch(pathToApp + '/styles/**/*', (done) => {
    gulp.series(['clean', 'styles'])(done);
  });
});

gulp.task('clean', () => {
  return del([
    pathToApp + '/index.css',
  ]);
});

gulp.task('default', gulp.series(['styles', 'watch']));
