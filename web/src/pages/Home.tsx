import { Typography } from "@material-tailwind/react";

export default function Home() {
    return (
        <div className="home">
            <Typography
                as="big"
                variant="h1"
                className="text-5xl mr-4 cursor-pointer py-1.5 font-normal">
                    WELCOME BACK.
            </Typography>

            <br />

            <Typography
                as="big"
                variant="h1"
                className="text-3xl mr-4 cursor-pointer py-1.5 font-normal">
                    <span className="bg-purple-600 text-white p-1 rounded">Our app</span> brings an <span className="bg-yellow-600 text-white p-1 rounded">unique set</span> of features that combines <span className="underline underline-offset-4 decoration-purple-600">social media</span>, <span className="underline underline-offset-4 decoration-purple-600">video editing platform</span>, and a <span className="underline underline-offset-4 decoration-purple-600">daily habit tracker</span>.
            </Typography>

            <br />

            <button type="button" className="inline-block mt-2 px-6 py-2 border-2 border-gray-800 text-gray-800 text-xs font-bold leading-tight uppercase rounded-full hover:bg-black hover:bg-opacity-5 focus:outline-none focus:ring-0 transition duration-150 ease-in-out">
                <span>Development in Progress</span>
            </button>
            
            <img src="screenshot.png" alt="screenshot" className="mt-5" />
        </div>
    );
}