# Slovíčky
Learn new words! 🧠

link in Google Play (not active at release) https://play.google.com/store/apps/details?id=com.abc.qwert.slovicky




## How did it start?

The idea of the application came from the need to facilitate the process of memorizing new words. Of course, there were many similar applications before that, but it seemed to be more convenient to use my own one.

![Image alt](https://github.com/vasshil/Slovicky/raw/master/screenshots/1.png)


## Realization

###### The main screen of the app is an endless stack of cards with words, input fields, hints, and some control buttons.
###### All this things works with the `ViewPager` using the `ViewPager.PageTransformer` and `InfiniteViewPager` libraries.
###### Unfortunately, I haven't found a good way to delete a single `ViewPager` element, or dynamically change it, so every such time I have to recreate the `ViewPagerAdapter`, but it doesn't take much time, since the number of visible `View`'s on the screen is only 3. Therefore, `instantiateItem()` is called exactly that many times.
>Of course, I tried using `ViewPager2` with `RecyclerView.Adapter`, but the visual response and performance did not meet the required level, so I had to leave the old `ViewPager`
###### If the user enters a correct or incorrect answer, the text input field changes its color to green or red, and also plays an animation. You can also just peek at the translation without entering anything.

![Image alt](https://github.com/vasshil/Slovicky/raw/master/screenshots/2.png)


Therefore, first of all, I decided to add this program to my application 💁‍♂️.
And here you can see how it looks like 👀:

![Image alt](https://github.com/vasshil/TheScience/raw/main/screenshots/6.png)




## Old plans

But my main task at that time was to create a physical calculator, where you could enter all the values we know and get everything that could be calculated from it. It turned out to be a very difficult task 🥵, but after many sleepless nights, I finally managed to do it 💪. 

![Image alt](https://github.com/vasshil/TheScience/raw/main/screenshots/2.png)

![Image alt](https://github.com/vasshil/TheScience/raw/main/screenshots/3.png)

In general, it turned out very well for a beginner, both in design and functionality. Of course, many things could have been done better, but I went further 🤟.




## What's next?

At first, I thought for a long time what else could be added to this application 🤔 and finally I came up with another idea 💡. We started to learn new topics in school about derived functions and functions with parameters 📈. So these 2 things became the next features of the Next app.
Thanks to the experience gained earlier, the implementation of this was not so difficult 🤓, but there were great difficulties with the implementation of function calculations. I eventually found the symja library for myself, as well as GraphView for working with MathJax 🤝.

And here's what finally happened 🤲:

![Image alt](https://github.com/vasshil/TheScience/raw/main/screenshots/4.png)

![Image alt](https://github.com/vasshil/TheScience/raw/main/screenshots/5.png)

At this moment, I decided to finish working on the project and put it on Google Play 🛒. Also, I initially added some ads to it 🤑, but then decided that it didn't make sense and turned it off 🤧.




## Total:
During the year of development, I got an incredible experience working on the project 👨🏻‍💻, I learned how to create my own applications completely from scratch and upload them to the Internet 🕸. Many of the similar applications on the Internet seemed to me rather nondescript and unattractive 😬, so during development I paid a lot of attention not only to the code itself, but also to the design, which would immediately draw the attention of a stranger to itself. 👨🏻‍🎨. As it seems to me, it turned out quite well, although even I will not be able to deal with that old code 😎. However, I took into account all the mistakes and did not repeat them in future projects 🥳
