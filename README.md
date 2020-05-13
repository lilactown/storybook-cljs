# storybook example

This repo is an example of integrating the latest storybook (v5) with shadow-cljs.

It makes use of the `:npm-module` build target.

## Running it

### Development

Open two terminals. In one, start shadow-cljs' compilation process:

```bash
npx shadow-cljs watch stories
```

In the other, start storybook's compilation process:

```bash
npm run storybook
```

It should automatically open a browser to http://localhost:6006.

If you see errors about "goog.math.Long" on hot reload, do a hard refresh and it
should resolve for the duration of your session.


### Releasing as a static site

Run the following commands:

1. Build the CLJS code first

```
$ npx shadow-cljs release stories
shadow-cljs - config: /Users/lilactown/Code/storybook/shadow-cljs.edn
shadow-cljs - connected to server
[:stories] Compiling ...
[:stories] Build completed. (45 files, 0 compiled, 0 warnings, 2.69s)
```

2. Build the storybook static site

```
$ npm run build-storybook

> storybook@1.0.0 build-storybook /Users/lilactown/Code/storybook
> build-storybook

info @storybook/react v5.3.18
...
info => Output directory: /Users/lilactown/Code/storybook/storybook-static
```
3. The above command by default outputs the static assets to `storybook-static`. We can validate this by starting an http server and viewing it in a browser


```
$ npx http-server storybook-static
Starting up http-server, serving storybook-static
Available on:
  http://127.0.0.1:8080
  http://192.168.0.150:8080
  http://10.255.255.247:8080
Hit CTRL-C to stop the server
```


## Reproducing

Here are the steps for reproducing this example project yourself.

### Initialize shadow-cljs and storybook

1. Create a new directory for our project

```
$ mkdir design-library
```

2. Create a `package.json` and install `react` and `react-dom`

```
$ npm init

$ npm i react react-dom
```

3. Install shadow-cljs as a dev dependency and create a basic `shadow-cljs.edn` file

```
$ npm i -D shadow-cljs

$ npx shadow-cljs init
shadow-cljs - init
- /Users/lilactown/Code/storybook2/shadow-cljs.edn
Create? [y/n]: y
shadow-cljs - created default configuration
```

4. Follow the instructions at https://storybook.js.org/docs/guides/quick-start-guide/ . As of this writing:

```
$ npx -p @storybook/cli sb init

 sb init - the simplest way to add a storybook to your project.

...
To run your storybook, type:

   npm run storybook

For more information visit: https://storybook.js.org
```

### Configuring shadow-cljs

We're going to use shadow-cljs [:npm-module](https://shadow-cljs.github.io/docs/UsersGuide.html#target-npm-module) build target. First, let's update our shadow-cljs.edn to reflect our project structure.

```clojure
{:source-paths
 ["src"]

 :dependencies
 []

 :builds
 {}}
```


Now let's create a `:stories` build that will use a `stories.core` namespace as
an entry point.

```clojure
{:source-paths
 ["src"]

 :dependencies
 []

 :builds
 {:stories {:target :npm-module
            :entries [stories.core]
            :output-dir "out"
            :devtools {:enabled false}}}}
```

We're going to output our generated JS files to an `out` directory inside our
project. We disbale `:devtools` because storybook handles hot reloading our code
in the browser.

Next, let's create a simple `stories.core` namespace.

```
$ mkdir -p src/stories

$ echo '(ns stories.core)' > src/stories/core.cljs
```

We can test that we've got things working by trying to compile with shadow-cljs.

```
$ npx shadow-cljs compile stories
shadow-cljs - config: /Users/lilactown/Code/storybook/shadow-cljs.edn
shadow-cljs - updating dependencies
shadow-cljs - dependencies updated
[:stories] Compiling ...
[:stories] Build completed. (40 files, 40 compiled, 0 warnings, 6.00s)
```

### Configuring storybook

Storybook stores it's configuration in the `.storybook` directory. We need to
make a couple of edits in order to get it to work correctly with shadow-cljs.

First, let's tell it to look in our `out` directory for stories.

In `.storybook/main.js`, add a glob that will look for any JS file ending in
`_stories.js` to the `stories` export:

```javascript
module.exports = {
  stories: ["../stories/**/*.stories.js", "../out/**/*_story.js"],
  addons: ["@storybook/addon-actions", "@storybook/addon-links"],
};
```

We also need to tell babel to to output our code as commonjs, as our CLJS code
are going to directly emit `module.exports`. We do this by creating a `.babelrc`
inside of `.storybook` with the following contents:

```javascript
{
  "presets": [
    ["@babel/preset-env", {"modules": "commonjs"}],
    "@babel/react"
  ]
}
```

### Validating it end-to-end

We should be all setup to start developing some stories. Let's add a little bit
of code to ensure that our project is working.

Create a new file called `corp/design/button.cljs` and put in it a simple `prn`.
Feel free to replace `corp` with whatever seems applicable. ;)

```clojure
(ns corp.design.button)

(prn "hi")
```

Then, let's require it in our `stories.core` namespace so that it gets built:

```clojure
(ns stories.core
  (:require [corp.design.button]))
```

In two terminals, start up our builds:

```
$ npx shadow-cljs watch stories
shadow-cljs - config: /Users/lilactown/Code/storybook/shadow-cljs.edn
[:stories] Compiling ...
[:stories] Build completed. (40 files, 40 compiled, 0 warnings, 6.00s)
```

and 

```
$ npm run storybook
> storybook@1.0.0 storybook /Users/lilactown/Code/storybook
> start-storybook -p 6006

info @storybook/react v5.3.18
...
webpack built a9ee4dc7a8c7778e2536 in 5199ms
╭────────────────────────────────────────────────────╮
│                                                    │
│   Storybook 5.3.18 started                         │
│   6.46 s for manager and 6.02 s for preview        │
│                                                    │
│    Local:            http://localhost:6006/        │
│    On your network:  http://192.168.0.150:6006/    │
│                                                    │
╰────────────────────────────────────────────────────╯
```

A browser window should open to http://localhost:6006 . If we open up our dev
console, we should see `"hi"` printed. If so, success!


### Developing components and stories

See the rest of the repo for examples of how to structure your files and create
a basic component.
