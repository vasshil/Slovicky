# SlovÃ­Äky
Learn new words! ð§ 

link in Google Play (not active at release) https://play.google.com/store/apps/details?id=com.abc.qwert.slovicky




## How did it start â±?

The idea of the application came from the need to facilitate the process of memorizing new words ð¡. Of course, there were many similar applications before that, but it seemed to be more convenient to use my own one ð¨ð»âð».

![Image alt](https://github.com/vasshil/Slovicky/raw/master/screenshots/1.png)


## Realization ð©ð 

The main screen of the app is an endless stack of cards with words, input fields, hints, and some control buttons ð.

All this things works with the `ViewPager` using the `ViewPager.PageTransformer` and `InfiniteViewPager` libraries ð.

Unfortunately, I haven't found a good way to delete a single `ViewPager` element, or dynamically change it, so every such time I have to recreate the `ViewPagerAdapter` ðª, but it doesn't take much time, since the number of visible `View`'s on the screen is only 3. Therefore, `instantiateItem()` is called exactly that many times. ð¤

>Of course, I tried using `ViewPager2` with `RecyclerView.Adapter`, but the visual response and performance did not meet the required level, so I had to leave the old `ViewPager` ð

If the user enters a correct or incorrect answer, the text input field changes its color to green or red, and also plays an animation. You can also just peek at the translation without entering anything. ð

Also, in future updates, you will be able to voice the words. ð

![Image alt](https://github.com/vasshil/Slovicky/raw/master/screenshots/2.png)

At the bottom of the screen there is a slider for easy scrolling of cards and also a button for shuffling words. ð¤©

#### Words âð»

For convenience, all words are divided into groups, so you can save your words separately and learn only what you need, and also the app will automatically create a group of all words. You can open the list of all groups directly under the app name. â¡ï¸

To make working with the app seem airy ð¬, I specifically didn't create a lot of `Activity`, instead, `BottomSheetDialog` was most often used ð¤ 

![Image alt](https://github.com/vasshil/Slovicky/raw/master/screenshots/3.png)


#### Notifications ð

An important part of the app is the notifications that will be sent every day at the user's chosen time from pre-marked words. âï¸

The time can be selected either at a certain interval or manually, so this method is perfect.ð¥°

Also, due to too much content, I had to create a new `Activity` for this. ð¥¸

![Image alt](https://github.com/vasshil/Slovicky/raw/master/screenshots/4.png)



#### Import/Export groups ð

In the app settings ð, there are 2 buttons for both importing and exporting a group of words. The group that is always selected at the moment is exported to the `SlovÃ­Äky` folder in the internal memory of the phone in the form `.zip` archive ð½. To import words, a new `Activity` with a file manager was also created, where only the folders and `.zip` files are visible. ð

![Image alt](https://github.com/vasshil/Slovicky/raw/master/screenshots/5.png)

![Image alt](https://github.com/vasshil/Slovicky/raw/master/screenshots/6.png)


## Total:

As a result, I can say that I was able to achieve all the goals set before the creation, and even more of that ð¥³! I am especially happy with the design and stability of the app ð¤¯. And most importantly, I have gained a lot of experience in working on it for several months ð¨ð»âð.
